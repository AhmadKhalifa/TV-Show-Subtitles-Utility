import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileWriter
import java.lang.Exception
import java.lang.RuntimeException
import java.nio.charset.Charset

object SubtitlesUtil {

    private const val DEFAULT_SUBTITLES_EXTENSION = "srt"

    /**
     * Used for an episode or a movie
     */
    fun changeEncodingForEpisodeOrMovie(
        subtitlesFile: File, episodeFile: File,
        subtitlesExtension: String = DEFAULT_SUBTITLES_EXTENSION,
        encoding: Charset = Charset.forName("Windows-1256")
    ) {
        val newSubtitlesFilePath = episodeFile.run {
            "${parentFile.absolutePath}\\${name.run { substring(0, lastIndexOf('.')) }}.$subtitlesExtension"
        }
        FileWriter(newSubtitlesFilePath).use { fileWriter ->
            fileWriter.write(FileUtils.readFileToString(subtitlesFile, encoding.displayName()))
        }
    }

    /**
     * Used for a season folder
     */
    fun changeEncodingForSeason(
        seasonPath: String, seasonSubtitlesPath: String,
        subtitlesExtension: String = DEFAULT_SUBTITLES_EXTENSION
    ) {
        println("Changing files encoding in folder $seasonSubtitlesPath to folder $seasonPath")
        val episodes = File(seasonPath).listFiles()
            ?.filter { file -> file.extension !in arrayOf("srt", "sub") } ?: arrayListOf()
        val subtitles = File(seasonSubtitlesPath).listFiles()
            ?.filter { file -> file.extension in arrayOf("srt", "sub") } ?: arrayListOf()
        if (episodes.size != subtitles.size) {
            throw RuntimeException(
                "Not equal number of episodes and subtitles: " +
                        "Found ${episodes.size} episodes and ${subtitles.size} subtitles"
            )
        }
        repeat(episodes.size) { index ->
            val episodeFile = episodes[index]
            val subtitlesFile = subtitles[index]
            val subtitlesFilePath = subtitlesFile.absolutePath.toString()
            println(" - Changing file encoding for file $subtitlesFilePath")
            try {
                changeEncodingForEpisodeOrMovie(subtitlesFile, episodeFile, subtitlesExtension)
            } catch (exception: Exception) {
                System.err.println(" - Error creating subtitles file from '$subtitlesFilePath': ${exception.message}")
                exception.printStackTrace()
            }
        }
        println("Done\n")
    }

    /**
     * Used for a folder containing seasons sub-folders like TV shows
     */
    fun changeEncodingForTvShow(
        tvShowPath: String, tvShowSubtitlesPath: String,
        subtitlesExtension: String = DEFAULT_SUBTITLES_EXTENSION
    ) {
        val episodesFolders = File(tvShowPath).listFiles()?.filter { it.isDirectory } ?: arrayListOf()
        val subtitlesFolders = File(tvShowSubtitlesPath).listFiles()?.filter { it.isDirectory } ?: arrayListOf()
        if (episodesFolders.size != subtitlesFolders.size) {
            throw RuntimeException(
                "Not equal number of episodes folders and subtitles folders: " +
                        "Found ${episodesFolders.size} episodes folders and ${subtitlesFolders.size} subtitles folders"
            )
        }
        repeat(episodesFolders.size) { index ->
            changeEncodingForSeason(
                episodesFolders[index].absoluteFile.toString(),
                subtitlesFolders[index].absoluteFile.toString(),
                subtitlesExtension
            )
        }
    }
}