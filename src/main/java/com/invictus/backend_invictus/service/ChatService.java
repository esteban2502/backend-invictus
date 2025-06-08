package com.invictus.backend_invictus.service;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final WebClient webClient;

    private static  String baseUrl;

    private static String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();



    public ChatService(WebClient.Builder webClientBuilder,  @Value("${gemini.api.base-url}") String baseUrl,
                       @Value("${gemini.api.key}") String apiKey
                       ) {
        this.apiKey = apiKey;
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }


    public Mono<String> chat(String prompt) {


        String systemPrompt = """
        Eres un experto en economía, criptomonedas y mercados financieros.
        Solo debes responder preguntas que estén relacionadas con estos temas.
        Si te hacen una pregunta fuera de contexto, responde:
        "Lo siento, solo puedo responder preguntas sobre criptomonedas y mercados financieros."
        
        darle prioridad a las siguientes palabras 
                   // Finanzas generales
                                "finanzas", "financiero", "financiera", "financiamiento", "financiar",
                                "finance", "financial", "funding", "loan", "credit", "debt", "mortgage",
                        
                                // Bancos
                                "banca", "banco", "bank", "banking", "central bank", "reserva federal", "federal reserve",
                        
                                // Inversión y mercados
                                "inversión", "invertir", "inversiones", "inversionista", "rentabilidad",
                                "investment", "invest", "investor", "profitability",
                        
                                // Acciones y bolsa
                                "acciones", "mercado", "mercados", "bolsa", "mercado de valores", "índice",
                                "stock", "stocks", "stock market", "shares", "nasdaq", "dow jones", "s&p 500", "nyse",
                        
                                // Criptomonedas
                                "cripto", "criptomoneda", "criptomonedas", "cryptocurrency", "cryptocurrencies",
                                "bitcoin", "btc", "ethereum", "eth", "litecoin", "ltc", "ripple", "xrp",
                                "dogecoin", "doge", "cardano", "ada", "bnb", "binance", "usdt", "usdc", "altcoin", "altcoins",
                                "crypto", "cryptos",
                        
                                // Blockchain
                                "blockchain", "cadena de bloques", "token", "tokens", "nft", "nfts",
                                "wallet", "billetera", "cold wallet", "hot wallet", "metamask", "ledger",
                        
                                // Intercambio y trading
                                "exchange", "intercambio", "intercambios", "trading", "trade", "trader", "brokers", "broker",
                        
                                // Economía
                                "economía", "económico", "economía global", "economics", "macro", "micro",
                                "macroeconomía", "microeconomía", "gdp", "pib", "inflación", "recesión", "crisis", "deflación",
                        
                                // Divisas y cambio
                                "divisas", "forex", "cambio de divisas", "tipo de cambio", "currency", "currencies",
                                "usd", "eur", "jpy", "moneda", "monedas", "exchange rate",
                        
                                // Indicadores financieros
                                "interés", "tasa de interés", "interest", "interest rate", "apr", "roi",
                                "dividendos", "dividends", "capital", "patrimonio", "valuation", "valoración",
                        
                                // Productos financieros
                                "bonos", "renta fija", "fondos", "fondos mutuos", "fondos indexados", "etf", "etfs",
                                "mutual funds", "index funds", "preferred stock", "acciones preferentes",
                        
                                // Riesgo y análisis
                                "riesgo", "volatilidad", "análisis técnico", "análisis fundamental", "indicadores", "gráfico",
                                "technical analysis", "fundamental analysis", "chart", "indicators", "trend",
                        
                                // Fintech y startups
                                "fintech", "startup", "capital de riesgo", "venture capital", "seed funding", "serie a", "serie b",
                        
                                // Regulaciones
                                "regulación", "compliance", "impuestos", "irs", "sec", "cnmv", "ley financiera", "tax", "regulatory",
                        
                                // Otros
                                "ahorro", "presupuesto", "liquidez", "cash flow", "flujo de caja", "capitalización",
                                "pricing", "market cap", "valor", "cotización", "buy", "sell", "precio", "price"
        """;

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(Map.of(
                        "contents", new Object[] {
                                Map.of("parts", new Object[] {
                                        Map.of("text", systemPrompt),
                                        Map.of("text", prompt)
                                })
                        }
                ))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(json -> {
                    try {
                        JsonNode root = objectMapper.readTree(json);
                        String text = root.path("candidates").get(0)
                                .path("content")
                                .path("parts").get(0)
                                .path("text")
                                .asText();
                        return Mono.just(text);
                    } catch (Exception e) {
                        return Mono.just("Error leyendo respuesta: " + e.getMessage());
                    }
                })
                .onErrorResume(e -> Mono.just("Error al contactar el modelo: " + e.getMessage()));
    }








}
