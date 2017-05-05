# SafeMediaPlayer
a wrap for MediaPlayer to make it easy and safe to use



# feature

* only one java class + two interfaces 
* no need to care about  the state
* no crash any more 
* automatically handle the OnAudioFocusChangeListener and headset/bluetooth plug out event 

# usage

## init

```
manager =  AudioPlayerManager.get(appcontext);
manager.setDataSource(url1)// for audio ,the datasource class type is string
		.setCallback(callback)
		.setStartPosition(60000)//start playing at 60s
```

## action/method of AudioPlayerManager

> the method is safe  to invoke ,no need to wory about the state of MediaPlayer

```
void start(); 
void start(Object dataSource);// when change the audio source
void pause();
void resume();
void stop();
void seekTo(int duration);
void release();//release the mediaplayer ,but remains the AudioPlayerManager
void releaseEveryThing();//release the mediaplayer and the AudioPlayerManager instance, and unregisterHeadsetPlugReceiver.

```

## callback

> dataSource : to identify the callback if in listview
>
> AudioPlayerManager : pass through the object for convienent useage

```
void onPreparing(Object dataSource, AudioPlayerManager manager);
void onPlaying(Object dataSource, AudioPlayerManager manager);
void onPause(Object dataSource, AudioPlayerManager manager);
void onCompletion(Object dataSource, AudioPlayerManager manager);
void onStop(Object dataSource, AudioPlayerManager manager);
void onError(String msg,Object dataSource, AudioPlayerManager manager);
void onRelease(Object dataSource, AudioPlayerManager manager);

void onGetMaxDuration(int maxDuration);
void onProgress(int progress, Object dataSource, AudioPlayerManager manager);
void onSeeking(Object dataSource, AudioPlayerManager manager);
void onBufferingUpdate(int percent, AudioPlayerManager manager);
```

# gradle

## gradle

**Step 1.** Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

```
    allprojects {
        repositories {
            ...
            maven { url "https://jitpack.io" }
        }
    }
```

**Step 2.** Add the dependency

```
    dependencies {
            compile 'com.github.hss01248:SafeMediaPlayer:lastest release'
    }
```

lastest release:https://github.com/hss01248/SafeMediaPlayer/releases

# todo 

VideoPlayerManager