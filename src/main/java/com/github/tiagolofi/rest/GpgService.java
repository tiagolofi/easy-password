package com.github.tiagolofi.rest;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@ApplicationScoped
public class GpgService {

    public byte[] encrypt(byte[] content, String filename, String passphrase) throws IOException, InterruptedException {

        Path directory = Files.createTempDirectory("gpg-");

        Path input = directory.resolve(filename);
        Path output = directory.resolve(filename + ".gpg");

        try {
            Files.write(input, content);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "gpg",
                    "--batch",
                    "--yes",
                    "--pinentry-mode", "loopback",
                    "--passphrase-fd", "0",
                    "--symmetric",
                    "--cipher-algo", "AES256",
                    "--output", output.toString(),
                    input.toString()
            );

            Process process = processBuilder.start();

            // A passphrase é enviada pelo stdin.
            // Assim não aparece nos argumentos do processo.
            try (var stdin = process.getOutputStream()) {
                stdin.write(
                        (passphrase + System.lineSeparator())
                                .getBytes(StandardCharsets.UTF_8)
                );
            }

            String error;

            try (var stderr = process.getErrorStream()) {
                error = new String(
                        stderr.readAllBytes(),
                        StandardCharsets.UTF_8
                );
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IllegalStateException(
                        "Erro ao executar GPG: " + error
                );
            }

            return Files.readAllBytes(output);

        } finally {
            Files.deleteIfExists(output);
            Files.deleteIfExists(input);
            Files.deleteIfExists(directory);
        }
    }
}