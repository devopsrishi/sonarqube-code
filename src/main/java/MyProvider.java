package madhu;

import com.github.bjoernpetersen.jmusicbot.InitStateWriter;
import com.github.bjoernpetersen.jmusicbot.InitializationException;
import com.github.bjoernpetersen.jmusicbot.Loggable;
import com.github.bjoernpetersen.jmusicbot.PlaybackFactoryManager;
import com.github.bjoernpetersen.jmusicbot.Song;
import com.github.bjoernpetersen.jmusicbot.config.Config;
import com.github.bjoernpetersen.jmusicbot.platform.Platform;
import com.github.bjoernpetersen.jmusicbot.platform.Support;
import com.github.bjoernpetersen.jmusicbot.playback.PlaybackFactory;
import com.github.bjoernpetersen.jmusicbot.provider.NoSuchSongException;
import com.github.bjoernpetersen.jmusicbot.provider.Provider;
import com.github.bjoernpetersen.mp3Playback.Mp3PlaybackFactory;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.sound.sampled.UnsupportedAudioFileException;

public class MyProvider implements Loggable, Provider {

  private Config.StringEntry exampleConfigEntry;
  private Mp3PlaybackFactory playbackFactory;

  @Nonnull
  @Override
  public Class<? extends Provider> getBaseClass() {
    return MyProvider.class;
  }

  @Nonnull
  @Override
  public Support getSupport(@Nonnull Platform platform) {
    switch (platform) {
      case ANDROID:
      case LINUX:
      case WINDOWS:
        return Support.YES;
      case UNKNOWN:
      default:
        return Support.MAYBE;
    }
  }

  @Nonnull
  @Override
  public List<? extends Config.Entry> initializeConfigEntries(@Nonnull Config config) {
    exampleConfigEntry = config.stringEntry(
        getClass(),
        "example",
        "This is an example config entry",
        "example-default-value",
        value -> {
          // check for valid value
          if (value.contains("example")) {
            return null;
          } else {
            return "Value must contain example";
          }
        }
    );
    return Collections.singletonList(exampleConfigEntry);
  }

  @Override
  public void destructConfigEntries() {
    exampleConfigEntry.tryDestruct();
    exampleConfigEntry = null;
  }

  @Override
  public Set<Class<? extends PlaybackFactory>> getPlaybackDependencies() {
    return Collections.singleton(Mp3PlaybackFactory.class);
  }

  @Override
  public void initialize(@Nonnull InitStateWriter initStateWriter,
      @Nonnull PlaybackFactoryManager manager) throws InitializationException {
    initStateWriter.state("Initializing...");
    playbackFactory = manager.getFactory(Mp3PlaybackFactory.class);

    // TODO initialize your resources here
  }

  @Override
  public void close() throws IOException {
    playbackFactory = null;

    // TODO close your resources here
  }

  @Nonnull
  private Song createExampleSong() {
    return new Song.Builder()
        .songLoader(song -> {
          // TODO you could download the song here, you could also pass the SongLoader.DUMMY
          try {
            return new File(song.getId()).createNewFile();
          } catch (IOException e) {
            return false;
          }
        })
        .playbackSupplier(song -> {
          try {
            return playbackFactory.createPlayback(new File(song.getId()));
          } catch (UnsupportedAudioFileException e) {
            throw new IOException(e);
          }
        })
        .provider(this)

        .id("example")
        .title("Example song")
        .description("this is an example song")
        // optional
        .albumArtUrl("http://via.placeholder.com/350x150")
        // optional
        .duration(180)
        .build();
  }

  @Nonnull
  @Override
  public List<Song> search(@Nonnull String query) {
    return Collections.singletonList(createExampleSong()); // TODO return search results
  }

  @Nonnull
  @Override
  public Song lookup(@Nonnull String songId) throws NoSuchSongException {
    if (songId.equals("example")) {
      return createExampleSong();
    } else {
      throw new NoSuchSongException();
    }
  }

  @Nonnull
  @Override
  public String getId() {
    return "example";
  }

  @Nonnull
  @Override
  public String getReadableName() {
    return "Example Songs";
  }
}
