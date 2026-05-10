package com.github.tiagolofi.filters;

import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.jboss.logging.Logger;

import com.github.tiagolofi.repository.Logs;
import com.github.tiagolofi.repository.LogsRepository;
import com.github.tiagolofi.repository.Metadata;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;

/**
 * Filter que captura informações detalhadas sobre os usuários que acessam a página /love-calendar.
 * Registra dados como IP, User-Agent, headers, localização geográfica e informações da máquina servidor.
 */
@Provider
public class LogAccessFilter implements ContainerRequestFilter {

    @Inject
    Logger logger;

    @Inject
    LogsRepository logsRepository;

    private String serverID;
    private String userAgent;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        logUserAccess(requestContext);
    }

    /**
     * Registra informações detalhadas sobre o acesso do usuário
     */
    private void logUserAccess(ContainerRequestContext requestContext) {
        if (serverID == null) {
            serverID = getServerIdentifier();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo().getRequestUri().toString();
        String path = requestContext.getUriInfo().getPath();
        String clientIP = getClientIP(requestContext);
        userAgent = requestContext.getHeaderString("User-Agent") != null ? requestContext.getHeaderString("User-Agent") : "NOT PROVIDED";
        String deviceInfo = getDeviceInfo(userAgent);
        String location = getLocationInfo(requestContext);
        String acceptLanguage = requestContext.getHeaderString("Accept-Language") != null ? requestContext.getHeaderString("Accept-Language") : "NOT PROVIDED";
        String referrer = requestContext.getHeaderString("Referer") != null ? requestContext.getHeaderString("Referer") : "DIRECT ACCESS";
        String acceptMediaTypes = requestContext.getAcceptableMediaTypes().toString();
        String queryParameters = !requestContext.getUriInfo().getQueryParameters().isEmpty() ? requestContext.getUriInfo().getQueryParameters().toString() : "NONE";
        
        // Todos os headers
        StringBuilder headersBuilder = new StringBuilder("{\n");
        requestContext.getHeaders().keySet().stream()
                .sorted()
                .forEach(headerName -> {
                    String headerValue = requestContext.getHeaderString(headerName);
                    headersBuilder.append("  ").append(headerName).append(": ").append(headerValue).append("\n");
                });
        headersBuilder.append("}");
        String headers = headersBuilder.toString();
        
        MediaType mediaType = requestContext.getMediaType();
        String contentType = mediaType != null ? mediaType.toString() : "NOT SPECIFIED";
        Long contentLength = (long) requestContext.getLength();

        // Log no console
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n========== LOG ACCESS FILTER ==========\n")
                .append("Timestamp: ").append(timestamp).append("\n")
                .append("Server ID: ").append(serverID).append("\n")
                .append("Method: ").append(method).append("\n")
                .append("URI: ").append(uri).append("\n")
                .append("Path: ").append(path).append("\n")
                .append("Client IP: ").append(clientIP).append("\n")
                .append("User-Agent: ").append(userAgent).append("\n")
                .append("Device Info: ").append(deviceInfo).append("\n")
                .append("Location: ").append(location).append("\n")
                .append("Accept-Language: ").append(acceptLanguage).append("\n")
                .append("Referrer: ").append(referrer).append("\n")
                .append("Accept Media Types: ").append(acceptMediaTypes).append("\n")
                .append("Query Parameters: ").append(queryParameters).append("\n")
                .append("Headers: ").append(headers).append("\n")
                .append("Content-Type: ").append(contentType).append("\n")
                .append("Content-Length: ").append(contentLength).append("\n")
                .append("=============================================\n");

        logger.infof(logMessage.toString());

        // Persiste no banco de dados
        try {
            Metadata metadata = new Metadata(
                timestamp,
                serverID,
                method,
                uri,
                path,
                clientIP,
                userAgent,
                deviceInfo,
                location,
                acceptLanguage,
                referrer,
                acceptMediaTypes,
                queryParameters,
                headers,
                contentType,
                contentLength
            );

            Logs logs = new Logs(
                LocalDateTime.now(ZoneId.of("America/Sao_Paulo")),
                metadata
            );

            logsRepository.persist(logs);
        } catch (Exception e) {
            logger.errorf("Erro ao persistir log: %s", e.getMessage());
        }
    }

    /**
     * Extrai o IP do cliente, considerando proxies e load balancers
     */
    private String getClientIP(ContainerRequestContext requestContext) {
        // Tenta vários headers que podem conter o IP real em ambientes com proxy
        String[] headerCandidates = {
            "X-Forwarded-For",
            "CF-Connecting-IP",
            "True-Client-IP",
            "X-Client-IP",
            "X-Real-IP"
        };

        for (String header : headerCandidates) {
            String ip = requestContext.getHeaderString(header);
            if (ip != null && !ip.isEmpty()) {
                // X-Forwarded-For pode conter múltiplos IPs, pega o primeiro
                return ip.split(",")[0].trim();
            }
        }

        // Fallback para remoteAddr se nenhum header for encontrado
        return requestContext.getSecurityContext() != null &&
               requestContext.getSecurityContext().getUserPrincipal() != null ?
               "Authenticated User" : "UNKNOWN";
    }

    /**
     * Obtém um identificador único da máquina servidor
     */
    private String getServerIdentifier() {
        try {
            // Combina hostname com um identificador único da JVM
            String hostname = InetAddress.getLocalHost().getHostName();
            // "-" + Integer.toHexString(System.identityHashCode(System.class));
            return hostname;
        } catch (Exception e) {
            return "SERVER-" + System.getenv("HOSTNAME");
        }
    }

    /**
     * Tenta extrair informações de localização do IP usando headers comuns
     */
    private String getLocationInfo(ContainerRequestContext requestContext) {
        StringBuilder location = new StringBuilder();

        // Headers que podem conter informações de localização (comuns em CDNs como Cloudflare)
        String[] locationHeaders = {
            "CF-IPCountry",     // Cloudflare: país
            "X-Country-Code",   // Código do país
            "X-City",           // Cidade
            "X-State",          // Estado/Província
            "X-Latitude",       // Latitude
            "X-Longitude",      // Longitude
            "X-Metro-Code"      // Código metropolitano
        };

        for (String header : locationHeaders) {
            String value = requestContext.getHeaderString(header);
            if (value != null && !value.isEmpty()) {
                if (location.length() > 0) {
                    location.append(", ");
                }
                location.append(header).append(": ").append(value);
            }
        }

        return location.length() > 0 ? location.toString() : "NOT AVAILABLE";
    }

    /**
     * Extrai informações do dispositivo/cliente a partir do User-Agent
     */
    private String getDeviceInfo(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "NOT PROVIDED";
        }

        StringBuilder deviceInfo = new StringBuilder();

        // Detecta SO
        if (userAgent.contains("Windows")) {
            deviceInfo.append("OS: Windows");
            if (userAgent.contains("Windows NT 10.0")) {
                deviceInfo.append(" 10/11");
            }
        } else if (userAgent.contains("Mac")) {
            deviceInfo.append("OS: macOS");
        } else if (userAgent.contains("Linux")) {
            deviceInfo.append("OS: Linux");
        } else if (userAgent.contains("Android")) {
            deviceInfo.append("OS: Android");
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            deviceInfo.append("OS: iOS");
        }

        deviceInfo.append(" | ");

        // Detecta browser
        if (userAgent.contains("Chrome")) {
            deviceInfo.append("Browser: Chrome");
        } else if (userAgent.contains("Firefox")) {
            deviceInfo.append("Browser: Firefox");
        } else if (userAgent.contains("Safari")) {
            deviceInfo.append("Browser: Safari");
        } else if (userAgent.contains("Edge")) {
            deviceInfo.append("Browser: Edge");
        } else if (userAgent.contains("OPR")) {
            deviceInfo.append("Browser: Opera");
        } else {
            deviceInfo.append("Browser: Unknown");
        }

        // Tipo de dispositivo
        if (userAgent.contains("Mobile")) {
            deviceInfo.append(" | Type: Mobile");
        } else if (userAgent.contains("Tablet")) {
            deviceInfo.append(" | Type: Tablet");
        } else {
            deviceInfo.append(" | Type: Desktop");
        }

        return deviceInfo.toString();
    }
}
