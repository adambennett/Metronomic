package pntanasis.android.metronome;

import android.os.Handler;
import android.os.Message;

/*
	- --
	- Tick-tock sound plays metronome at the wrong speed. Playing the beeping sound plays correctly.
	- --
	-
	- --

 */






public class Metronome {
	
	private double bpm;		// tempo
	private double beat;	// numerator of time signature
	private int noteValue;	// denominator of time signature
	private int silence;

	private double beatSound;
	private double sound;
	private int tick = 1000; // samples of tick
	
	private boolean play = true;
	
	private AudioGenerator audioGenerator = new AudioGenerator(8000);
	private Handler mHandler;
	private double[] soundTickArray;
	private double[] soundTockArray;
	private double[] silenceSoundArray;
	private byte[] tickArray;
	private byte[] tockArray;
	private byte[] silenceArray;
	private Message msg;
	private double currentBeat = 1;
	private double beatsPerBar;
	private double ratio = 1;
	private double ratio2 = 1;
	private int playedBeats = 0;
	private int id;
	private String messageString = "1";

	public Metronome(Handler handler) {
		audioGenerator.createPlayer();
		this.mHandler = handler;
		this.beatsPerBar = 4;
	}

	public void calcSilence() {
		ratio = beat/beatsPerBar;
		ratio2 = beatsPerBar/beat;
		//currentBeat = ratio;
		//System.out.println("ratio " + beatsPerBar);
		silence = (int) (((60/bpm)*8000)-tick);
		soundTickArray = new double[this.tick];
		soundTockArray = new double[this.tick];
		silenceSoundArray = new double[this.silence];
		msg = new Message();
		msg.obj = ""+currentBeat;
		double[] tick = audioGenerator.getSineWave(this.tick, 8000, beatSound);
		double[] tock = audioGenerator.getSineWave(this.tick, 8000, sound);
		for (int i = 0; i < this.tick; i++)
		{
			soundTickArray[i] = tick[i];
			soundTockArray[i] = tock[i];
		}
		for(int i=0;i<silence;i++)
			silenceSoundArray[i] = 0;

		tickArray = audioGenerator.get16BitPcm(soundTickArray);
		tockArray = audioGenerator.get16BitPcm(soundTockArray);
		silenceArray = audioGenerator.get16BitPcm(silenceSoundArray);
		/*
		System.out.println("Tick array: " + tickArray.length);
		System.out.println("Tock array: " + tockArray.length);
		System.out.println("Silence array: " + silenceArray.length);
		System.out.println("Silence sound array: " + silenceSoundArray.length);
		*/
	}

	public void calcSilenceRunning() {
		audioGenerator.createNewPlayer();
		ratio = beat/beatsPerBar;
		//currentBeat = ratio;
		//System.out.println("ratio " + beatsPerBar);
		int tick2 = tickArray.length;
		silence = (int) (((60/bpm)*44100)-tick2);
		silenceSoundArray = new double[this.silence];
		//msg = new Message();
		//msg.obj = ""+currentBeat;
		for(int i=0;i<silence;i++)
			silenceSoundArray[i] = 0;
		silenceArray = audioGenerator.get16BitPcm(silenceSoundArray);
		/*
		System.out.println("Tick array: " + tickArray.length);
		System.out.println("Tock array: " + tockArray.length);
		System.out.println("Silence array: " + silenceArray.length);
		System.out.println("Silence sound array: " + silenceSoundArray.length);
		*/
	}


	public void play() {
		//System.out.println("Beat: " + beat);
		//System.out.println("Beats per bar: " + beatsPerBar);
		currentBeat = 1;
		if (id == 1)
		{
			calcSilence();
			//System.out.println("Calc silence " + tockArray.length);
		}
		else
		{
			calcSilenceRunning();
			//System.out.println("Calc silence running " + tockArray.length);
		}
		do
		{
			msg = new Message();                            // beat counting number
			double currentBeat2 = currentBeat - ratio;
			if (currentBeat2 == 0) { currentBeat2 = beat; }
			if (currentBeat2 < 1 && currentBeat2 != 0) { currentBeat2 += beat; }
			if (currentBeat2 % 1 == 0 && currentBeat2 != 0)
			{
				messageString = "" + (int)currentBeat2;
			}
			else if ((currentBeat2 + 0.5) % 1 == 0) { messageString = "and"; }
			else if ((currentBeat2 + 0.75) % 1 == 0) { messageString = "e"; }
			else if ((currentBeat2 + 0.25) % 1 == 0) { messageString = "a"; }
			else if ((currentBeat2 + 2/3) % 1 == 0) { messageString = "trip"; }
			else if ((currentBeat2 + 1/3) % 1 == 0) { messageString = "let"; }
			else { messageString = "" + currentBeat2; }
			msg.obj = messageString;
			if (currentBeat == 1.0) { audioGenerator.writeSoundDirect(tockArray); }
			else { audioGenerator.writeSoundDirect(tickArray); }
			if(bpm <= 120) { mHandler.sendMessage(msg); }
			audioGenerator.writeSoundDirect(silenceArray);
			if(bpm > 120) { mHandler.sendMessage(msg); }
			currentBeat += ratio;
			playedBeats++;
			if(currentBeat > beat)
			{
				//System.out.println("ratio: " + ratio);
				//System.out.println("Played " + playedBeats + " beats\n");
				currentBeat = ratio;
				playedBeats = 0;
			}
		} while(play);
	}
	
	public void stop()
	{
		play = false;
		audioGenerator.destroyAudioTrack();
	}

	public void changeTockSound(byte[] sound)
	{
		tockArray = sound;
		this.id = 2;
	}

	public void changeTickSound(byte[] sound)
	{
		tickArray = sound;
		this.id = 2;
	}


	public double getBpm()
	{
		return bpm;
	}

	public void setBpm(int bpm)
	{
		this.bpm = bpm;
	}

	public void setNoteValue(int bpmetre)
	{
		this.noteValue = bpmetre;
	}

	public double getBeat()
	{
		return beat;
	}

	public void setBeat(double beat)
	{
		this.beat = beat;
	}

	public void setBeatSound(double sound1)
	{
		this.beatSound = sound1;
	}

	public void setSound(double sound2)
	{
		this.sound = sound2;
	}

	public void setBeatsPerBar(double bpb)
	{
		this.beatsPerBar = bpb;
		this.currentBeat = 1;
	}

	public void setAudioID(int id)
	{
		this.id = id;
	}

}
