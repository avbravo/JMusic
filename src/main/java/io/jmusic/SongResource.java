package io.jmusic;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/songs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SongResource {

    @Inject
    LyricService lyricService;

    private static final String STORAGE_DIR = "songs_data";

    public SongResource() {
        File dir = new File(STORAGE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @GET
    public List<String> listSongs() {
        try (Stream<java.nio.file.Path> stream = Files.list(Paths.get(STORAGE_DIR))) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(java.nio.file.Path::getFileName)
                    .map(java.nio.file.Path::toString)
                    .filter(name -> name.endsWith(".md"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @GET
    @Path("/{name}")
    public String getSong(@PathParam("name") String name) {
        try {
            return Files.readString(Paths.get(STORAGE_DIR, name));
        } catch (IOException e) {
            throw new NotFoundException();
        }
    }

    @POST
    public Response saveSong(Song song) {
        try {
            String fileName = song.name.endsWith(".md") ? song.name : song.name + ".md";
            Files.writeString(Paths.get(STORAGE_DIR, fileName), song.content);
            return Response.ok().build();
        } catch (IOException e) {
            return Response.serverError().build();
        }
    }

    @POST
    @Path("/analyze")
    @Consumes(MediaType.TEXT_PLAIN)
    public List<LyricService.VerseAnalysis> analyze(String content) {
        return lyricService.analyzeLyrics(content);
    }

    public static class Song {
        public String name;
        public String content;
    }
}
