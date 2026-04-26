package it.fulminazzo.blocksmith.minecraft.util

import java.util.concurrent.CompletableFuture

final class HttpRequestCatcher {
    private ServerSocket socket
    private CompletableFuture<List<String>> future

    List<String> getLines() {
        return future?.get()
    }

    HttpRequestCatcher start(int port) {
        socket = new ServerSocket(port)
        future = CompletableFuture.supplyAsync {
            def client = socket.accept()
            def lines = handleClient(client)

            client.close()

            socket.close()
            socket = null

            return lines
        }
        return this
    }

    void stop() {
        if (future != null) future.cancel(true)
        if (socket != null) socket.close()
    }

    private static List<String> handleClient(final Socket client) {
        def stream = client.inputStream.newReader()
        List<String> lines = []
        String current
        while ((current = stream.readLine()) != null && !current.empty)
            lines.add(current)
        return lines
    }

}
