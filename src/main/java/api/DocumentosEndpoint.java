/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api;

import support.HashtableOA;
import support.SingletonHashtableOA;
import entities.Documento;
import entities.Palabra;
import entities.Posteo;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import persistence.DocumentoDao;
import persistence.PalabraDao;
import persistence.PosteoDao;
import persistence.Util;
import support.Counter;
import support.ResponseJson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author lorenzofabro
 */
@Path("/api/documentos")
public class DocumentosEndpoint {

    @Inject
    private DocumentoDao daoDocumento;
    @Inject
    private PalabraDao daoPalabra;
    @Inject
    private PosteoDao daoPosteo;

    private SingletonHashtableOA singletonHashtableOA = SingletonHashtableOA.getInstance();
    private HashtableOA<String, Long> vocabulario = singletonHashtableOA.getHashtableOA();

    private char alfabeto[] = {'a', 'e', 'i', 'o', 'u', 'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'ñ', 'p', 'q',
        'r', 's', 't', 'v', 'w', 'x', 'y', 'z',
        'A', 'E', 'I', 'O', 'U', 'B', 'C', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'Ñ', 'P', 'Q',
        'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z',
        'á', 'é', 'í', 'ó', 'ú', 'Á', 'É', 'Í', 'Ó', 'Ú', 'ü', 'Ü',
        '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};

    @POST
    @Path("/indexar/carpeta")
    @Produces("application/json")
    public Response indexarCarpeta() throws FileNotFoundException {
        long startTime = System.nanoTime();
        ResponseJson respuesta = new ResponseJson();

        File folder = new File("documentos/");
        File[] listOfFiles = folder.listFiles();

        for (int f = 0; f < listOfFiles.length; f++) {
            if (listOfFiles[f].isFile()) {
                File file = listOfFiles[f];
                Documento documento = new Documento(listOfFiles[f].getName(), "documentos/ " + listOfFiles[f].getName());
                daoDocumento.create(documento);
                HashtableOA<String, Counter> palabrasNuevas = new HashtableOA<>();
                try ( Scanner sc = new Scanner(file, "ISO-8859-1")) {

                    while (sc.hasNext()) {

                        // Separo los posibles tokens de la cadena
                        String probableTokens = sc.next();
                        String tokens[] = probableTokens.split("[ -.]");

                        // Recorro y limpio los tokens encontrados y los almaceno en la tabla hash auxiliar
                        for (String token : tokens) {

                            // Limpio el token a partir del alfabeto
                            StringBuilder correctedToken = new StringBuilder();
                            for (int i = 0; i < token.length(); i++) {
                                char caracter = token.charAt(i);
                                for (char caracterAlfabeto : this.alfabeto) {
                                    if (caracter == caracterAlfabeto) {
                                        correctedToken.append(caracter);
                                        break;
                                    }
                                }
                            }

                            // Evito el guardado del caracter nulo
                            if (correctedToken.toString().equals("")) {
                                continue;
                            }

                            // Guardo palabra si no existe
                            if (!palabrasNuevas.containsKey(correctedToken.toString().toLowerCase())) {
                                palabrasNuevas.put(correctedToken.toString().toLowerCase(), new Counter());

                                // Aumento el contador si la palabra ya existia
                            } else {
                                palabrasNuevas.get(correctedToken.toString().toLowerCase()).increase();
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    respuesta.setStateResponse(ResponseJson.State.ERROR);
                    respuesta.setMessageResponse("No se pudo procesar el documento");
                    return Response.ok(respuesta.getResponseJson()).build();
                }

                // Proceso los tokens persistiendolos y agregandolos al vocabulario
                Set<Map.Entry<String, Counter>> set = palabrasNuevas.entrySet();
                ArrayList<Palabra> newWords = new ArrayList<>();
                ArrayList<Palabra> existingWords = new ArrayList<>();
                ArrayList<Posteo> newPosteos = new ArrayList<>();

                for (Map.Entry<String, Counter> entry : set) {
                    Palabra palabra;

                    if (!this.vocabulario.containsKey(entry.getKey())) {
                        palabra = new Palabra(entry.getKey(), 1, entry.getValue().getValue());
                        newWords.add(palabra);
                    } else {
                        long idPalabra = this.vocabulario.get(entry.getKey());
                        palabra = daoPalabra.retrieve(idPalabra);
                        palabra.addCantDocumentos();
                        if (entry.getValue().getValue() > palabra.getMaxFrecuencia()) {
                            palabra.setMaxFrecuencia(entry.getValue().getValue());
                        }
                        existingWords.add(palabra);
                    }
                }
                daoPalabra.insertArrayList(newWords);
                daoPalabra.updateArrayList(existingWords);

                for (Palabra palabra : newWords) {
                    this.vocabulario.put(palabra.getNombre(), palabra.getId());
                    Posteo posteo = new Posteo(palabra, documento, palabra.getMaxFrecuencia());
                    newPosteos.add(posteo);
                }

                for (Map.Entry<String, Counter> entry : set) {
                    for (Palabra palabra : existingWords) {
                        if (entry.getKey().equals(palabra.getNombre())) {
                            Posteo posteo = new Posteo(palabra, documento, entry.getValue().getValue());
                            newPosteos.add(posteo);
                            break;
                        }
                    }
                }

                daoPosteo.insertArrayList(newPosteos);

                System.out.println("Documento indexado => " + f);
            }
        }
        long endTime = System.nanoTime();
        long elapsedTime = (endTime - startTime);
        double elapsedTimeInSeconds = (double) elapsedTime / 1_000_000_000;
        double elapsedTimeInMinutes = elapsedTimeInSeconds / 60;

        System.out.println("[Elapsed time] => " + elapsedTimeInSeconds + " seconds");
        System.out.println("[Elapsed time] => " + elapsedTimeInMinutes + " minutes");

        respuesta.setStateResponse(ResponseJson.State.OK);
        respuesta.setMessageResponse("Documento indexado exitosamente");
        return Response.ok(respuesta.getResponseJson()).build();
    }

    //****************************** Metodo para indexado 
    // Método rápido con bulk insert/update
    @POST
    @Path("/indexar/bulk")
    @Produces("application/json")
    public Response indexarDocumentoNuevo(@FormParam("documento") String documentoBase64,
            @FormParam("titulo") String titulo) {

        long startTime = System.nanoTime();
        System.out.println("documento: " + documentoBase64);
        System.out.println("titulo:" + titulo);

        // Creo objeto respuesta
        ResponseJson respuesta = new ResponseJson();

        // Obtengo y almaceno el archivo del documento a partir del base64
        String rutaDocumento = "documentos/" + titulo;
        File fileDocumento = new File(rutaDocumento);
        byte[] bytesDocumento = Base64.getDecoder().decode(documentoBase64);
        try ( FileOutputStream outputStream = new FileOutputStream(fileDocumento)) {
            outputStream.write(bytesDocumento);
        } catch (Exception ex) {
            ex.printStackTrace();
            respuesta.setStateResponse(ResponseJson.State.ERROR);
            respuesta.setMessageResponse("No se pudo procesar el documento");
            return Response.ok(respuesta.getResponseJson()).build();
        }

        // Almaceno el documento en la base de datos
        Documento documento = new Documento(titulo, rutaDocumento);
        daoDocumento.create(documento);

        // Proceso el archivo guardando los tokens en una tabla hash auxiliar para luego persistirlas
        HashtableOA<String, Counter> palabrasNuevas = new HashtableOA<>();
        try ( Scanner sc = new Scanner(fileDocumento, "ISO-8859-1")) {

            while (sc.hasNext()) {

                // Separo los posibles tokens de la cadena
                String probableTokens = sc.next();
                String tokens[] = probableTokens.split("[ -.]");

                // Recorro y limpio los tokens encontrados y los almaceno en la tabla hash auxiliar
                for (String token : tokens) {

                    // Limpio el token a partir del alfabeto
                    StringBuilder correctedToken = new StringBuilder();
                    for (int i = 0; i < token.length(); i++) {
                        char caracter = token.charAt(i);
                        for (char caracterAlfabeto : this.alfabeto) {
                            if (caracter == caracterAlfabeto) {
                                correctedToken.append(caracter);
                                break;
                            }
                        }
                    }

                    // Evito el guardado del caracter nulo
                    if (correctedToken.toString().equals("")) {
                        continue;
                    }

                    // Guardo palabra si no existe
                    if (!palabrasNuevas.containsKey(correctedToken.toString().toLowerCase())) {
                        palabrasNuevas.put(correctedToken.toString().toLowerCase(), new Counter());

                        // Aumento el contador si la palabra ya existia
                    } else {
                        palabrasNuevas.get(correctedToken.toString().toLowerCase()).increase();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            respuesta.setStateResponse(ResponseJson.State.ERROR);
            respuesta.setMessageResponse("No se pudo procesar el documento");
            return Response.ok(respuesta.getResponseJson()).build();
        }

        // Cuento las palabras totales para llevar un porcentaje de las palabras almacenadas
        int totalTokens = palabrasNuevas.size();

        // Proceso los tokens persistiendolos y agregandolos al vocabulario
        Set<Map.Entry<String, Counter>> set = palabrasNuevas.entrySet();
        int persistTokens = 0;
        ArrayList<Palabra> newWords = new ArrayList<>();
        ArrayList<Palabra> existingWords = new ArrayList<>();
        ArrayList<Posteo> newPosteos = new ArrayList<>();
        for (Map.Entry<String, Counter> entry : set) {

            // Muestro porcentaje de palabras persistidas
            persistTokens++;
            System.out.println("Procesando palabras ==> " + persistTokens * 100 / totalTokens + "%");

            // Si la palabra no existe la guardo
            Palabra palabra;

            if (!this.vocabulario.containsKey(entry.getKey())) {
                palabra = new Palabra(entry.getKey(), 1, entry.getValue().getValue());
                newWords.add(palabra);
            } else {
                long idPalabra = this.vocabulario.get(entry.getKey());
                palabra = daoPalabra.retrieve(idPalabra);
                palabra.addCantDocumentos();
                if (entry.getValue().getValue() > palabra.getMaxFrecuencia()) {
                    palabra.setMaxFrecuencia(entry.getValue().getValue());
                }
                existingWords.add(palabra);
            }
        }
        System.out.println("Todas las palabras fueron procesadas :)");

        System.out.println("Iniciando bulk insert de palabras nuevas...");
        System.out.println(newWords);
        daoPalabra.insertArrayList(newWords);
        System.out.println("Iniciando bulk update de palabras existentes...");
        System.out.println(existingWords);
        daoPalabra.updateArrayList(existingWords);

        for (Palabra palabra : newWords) {
            this.vocabulario.put(palabra.getNombre(), palabra.getId());
            Posteo posteo = new Posteo(palabra, documento, palabra.getMaxFrecuencia());
            newPosteos.add(posteo);
        }

        for (Map.Entry<String, Counter> entry : set) {
            for (Palabra palabra : existingWords) {
                if (entry.getKey().equals(palabra.getNombre())) {
                    Posteo posteo = new Posteo(palabra, documento, entry.getValue().getValue());
                    newPosteos.add(posteo);
                    break;
                }
            }
        }

        System.out.println("Iniciando bulk insert de posteos...");
        System.out.println(newPosteos);
        daoPosteo.insertArrayList(newPosteos);

        System.out.println("Bulk insert/update finalizado :D");

        long endTime = System.nanoTime();
        long elapsedTime = (endTime - startTime);
        double elapsedTimeInSeconds = (double) elapsedTime / 1_000_000_000;

        System.out.println("[Elapsed time] => " + elapsedTimeInSeconds + " seconds");

        // Genero respuesta json
        respuesta.setStateResponse(ResponseJson.State.OK);
        respuesta.setMessageResponse("Documento indexado exitosamente");
        return Response.ok(respuesta.getResponseJson()).build();
    }

    // DEPRECADO ==> Versión lenta (sin hacer un bulk insert).
    @POST
    @Path("/indexar/comun ")
    @Produces("application/json")
    public Response indexarDocumento(@FormParam("documento") String documentoBase64,
            @FormParam("titulo") String titulo) {

        System.out.println("documento: " + documentoBase64);
        System.out.println("titulo:" + titulo);

        // Creo objeto respuesta
        ResponseJson respuesta = new ResponseJson();

        // Obtengo y almaceno el archivo del documento a partir del base64
        String rutaDocumento = "documentos/" + titulo;
        File fileDocumento = new File(rutaDocumento);
        byte[] bytesDocumento = Base64.getDecoder().decode(documentoBase64);
        try ( FileOutputStream outputStream = new FileOutputStream(fileDocumento)) {
            outputStream.write(bytesDocumento);
        } catch (Exception ex) {
            ex.printStackTrace();
            respuesta.setStateResponse(ResponseJson.State.ERROR);
            respuesta.setMessageResponse("No se pudo procesar el documento");
            return Response.ok(respuesta.getResponseJson()).build();
        }

        // Almaceno el documento en la base de datos
        Documento documento = new Documento(titulo, rutaDocumento);
        daoDocumento.create(documento);

        // Proceso el archivo guardando los tokens en una tabla hash auxiliar para luego persistirlas
        HashtableOA<String, Counter> palabrasNuevas = new HashtableOA<>();
        try ( Scanner sc = new Scanner(fileDocumento, "ISO-8859-1")) {

            while (sc.hasNext()) {

                // Separo los posibles tokens de la cadena
                String probableTokens = sc.next();
                String tokens[] = probableTokens.split("[ -.]");

                // Recorro y limpio los tokens encontrados y los almaceno en la tabla hash auxiliar
                for (String token : tokens) {

                    // Limpio el token a partir del alfabeto
                    StringBuilder correctedToken = new StringBuilder();
                    for (int i = 0; i < token.length(); i++) {
                        char caracter = token.charAt(i);
                        for (char caracterAlfabeto : this.alfabeto) {
                            if (caracter == caracterAlfabeto) {
                                correctedToken.append(caracter);
                                break;
                            }
                        }
                    }

                    // Evito el guardado del caracter nulo
                    if (correctedToken.toString().equals("")) {
                        continue;
                    }

                    // Guardo palabra si no existe
                    if (!palabrasNuevas.containsKey(correctedToken.toString().toLowerCase())) {
                        palabrasNuevas.put(correctedToken.toString().toLowerCase(), new Counter());

                        // Aumento el contador si la palabra ya existia
                    } else {
                        palabrasNuevas.get(correctedToken.toString().toLowerCase()).increase();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            respuesta.setStateResponse(ResponseJson.State.ERROR);
            respuesta.setMessageResponse("No se pudo procesar el documento");
            return Response.ok(respuesta.getResponseJson()).build();
        }

        // Cuento las palabras totales para llevar un porcentaje de las palabras almacenadas
        int totalTokens = palabrasNuevas.size();

        // Proceso los tokens persistiendolos y agregandolos al vocabulario
        Set<Map.Entry<String, Counter>> set = palabrasNuevas.entrySet();
        int persistTokens = 0;
        for (Map.Entry<String, Counter> entry : set) {

            // Muestro porcentaje de palabras persistidas
            persistTokens++;
            System.out.println("Almacenando palabras... " + persistTokens * 100 / totalTokens + "% procesadas");

            // Si la palabra no existe la guardo
            Palabra palabra;
            if (!this.vocabulario.containsKey(entry.getKey())) {
                palabra = new Palabra(entry.getKey(), 1, entry.getValue().getValue());
                daoPalabra.create(palabra);
                this.vocabulario.put(palabra.getNombre(), palabra.getId());

                // Si la palabra ya existia verifico la frecuencia maxima
            } else {
                long idPalabra = this.vocabulario.get(entry.getKey());
                palabra = daoPalabra.retrieve(idPalabra);
                palabra.addCantDocumentos();
                if (entry.getValue().getValue() > palabra.getMaxFrecuencia()) {
                    palabra.setMaxFrecuencia(entry.getValue().getValue());
                }
                daoPalabra.update(palabra);
            }
            // Guardo el nuevo posteo
            Posteo posteo = new Posteo(palabra, documento, entry.getValue().getValue());
            daoPosteo.create(posteo);

        }
        System.out.println("Todas las palabras fueron procesadas :)");
        // Genero respuesta json
        respuesta.setStateResponse(ResponseJson.State.OK);
        respuesta.setMessageResponse("Documento indexado exitosamente");
        return Response.ok(respuesta.getResponseJson()).build();
    }

    //****************************** Metodo para busqueda
    @GET
    @Path("/buscar")
    @Produces("application/json")
    public Response buscarDocumentos(@QueryParam("busquedaString") String busquedaString) {
        // Creo objeto respuesta
        ResponseJson respuesta = new ResponseJson();

        // Limpio la cadena de busqueda generando una lista con los objetos palabras ordenados por maxima frecuencia
        ArrayList<Palabra> palabras = new ArrayList<>();
        try ( Scanner sc = new Scanner(busquedaString)) {

            while (sc.hasNext()) {
                // Separo los posibles tokens de la cadena
                String probableTokens = sc.next();
                String tokens[] = probableTokens.split("[ -.]");

                // Recorro y limpio los tokens encontrados para agregarlos a la lista
                for (String token : tokens) {
                    // Limpio el token a partir del alfabeto
                    StringBuilder correctedToken = new StringBuilder();
                    for (int i = 0; i < token.length(); i++) {
                        char caracter = token.charAt(i);
                        for (char caracterAlfabeto : this.alfabeto) {
                            if (caracter == caracterAlfabeto) {
                                correctedToken.append(caracter);
                                break;
                            }
                        }
                    }

                    // Evito el procesamiento del caracter nulo
                    if (correctedToken.toString().equals("")) {
                        continue;
                    }

                    // Si la palabra existe en el vocabulario obtengo su objeto
                    Long idPalabra = this.vocabulario.get(correctedToken.toString().toLowerCase());

                    if (idPalabra != null) {
                        Palabra palabra = daoPalabra.retrieve(idPalabra);
                        System.out.println("[palabra] => " + palabra);
                        // Agrego a la lista ordenada
                        boolean insert = true;
                        Integer indexInsert = null;
                        for (int j = 0; j < palabras.size(); j++) {
                            if (palabras.get(j).getId() == palabra.getId()) {
                                insert = false;
                                break;
                            }
                            if (palabras.get(j).getMaxFrecuencia() <= palabra.getMaxFrecuencia()) {
                                continue;
                            }
                            indexInsert = j;
                            break;
                        }
                        if (insert) {
                            if (indexInsert == null) {
                                palabras.add(palabra);
                            } else {
                                palabras.add(indexInsert, palabra);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            respuesta.setStateResponse(ResponseJson.State.ERROR);
            respuesta.setMessageResponse("No se pudieron procesar las palabras de la cadena de búsqueda");
            return Response.ok(respuesta.getResponseJson()).build();
        }

        int cantidadDocumentos = daoDocumento.findAll().size();

        // Obtengo la cantidad minima de documentos asociados a cada palabra
        ArrayList<Posteo> selectedPosteos = new ArrayList<>();
        for (Palabra palabra : palabras) {
            int contadorDocumentos = 0;
            for (Posteo posteo : palabra.getPosteoCollection()) {
                if (contadorDocumentos >= cantidadDocumentos) {
                    break;
                }
                selectedPosteos.add(posteo);
                contadorDocumentos++;
            }
        }

        // Recorro los posteos para asociar un peso a cada documento y los guardo en una tabla hash auxiliar
        HashtableOA<Long, Double> weightDocumentos = new HashtableOA<>();
        for (Posteo posteo : selectedPosteos) {
            // Obtengo el id del documento asociado al posteo para usarlo como key en la tabla hash auxiliar
            long idDocumento = posteo.getDocumento().getId();

            // Calculo el peso del documento en base a todas las palabras de la busqueda
            double weight = 0;
            for (Palabra palabra : palabras) {
                Posteo posteoPalabraDocumento = daoPosteo.retrieve(palabra.getId(), idDocumento);
                double frecuenciaPalabraDocumento = (posteoPalabraDocumento != null)
                        ? posteoPalabraDocumento.getFrecuencia() : 0;
                double cantDocumentosPalabra = palabra.getPosteoCollection().size();
                weight += frecuenciaPalabraDocumento * Math.log(cantidadDocumentos / cantDocumentosPalabra);
//                System.out.println("[/////////////////////////////]");
//                System.out.println("[posteoPalabraDocumento] => " + posteoPalabraDocumento);
//                System.out.println("[palabra analizada] => " + palabra);
//                System.out.println("[cantDocumentosPalabra] => " + cantDocumentosPalabra);
//                System.out.println("[frecuenciaPalabraDocumento] => " + frecuenciaPalabraDocumento);
//                System.out.println("[weight] => " + weight);
//                System.out.println("[/////////////////////////////]");
            }
            // Obtengo el peso actual del documento en caso de que ya haya sido agregado a la tabla
            double actualWeight = 0;
            if (weightDocumentos.containsKey(idDocumento)) {
                actualWeight = weightDocumentos.get(idDocumento);
            }

            // Almaceno el documento en la tabla
            weightDocumentos.put(idDocumento, weight + actualWeight);
        }

//        System.out.println("[Weight documentos: ] =>" + weightDocumentos);
        // Genero lista ordenada de documentos basada en el peso de los mismos
        ArrayList<Documento> rankedDocumentos = new ArrayList<>();
        ArrayList<Double> pesos = new ArrayList<>();
        Set<Map.Entry<Long, Double>> set = weightDocumentos.entrySet();

        System.out.println("[Set (idDocumento, peso) ] => " + set);

        for (Map.Entry<Long, Double> entry : set) {

            // Obtengo objeto documento
            Documento documento = daoDocumento.retrieve(entry.getKey());
            // Agrego a la lista ordenada
            Integer indexInsert = null;
            for (int i = 0; i < rankedDocumentos.size(); i++) {
                double weightDocumentoInsert = entry.getValue();
                double weightDocumentoIterating = weightDocumentos.get(rankedDocumentos.get(i).getId());
                if (Double.compare(weightDocumentoInsert, weightDocumentoIterating) <= 0) {
                    continue;
                }
                indexInsert = i;
                break;
            }
            if (indexInsert == null) {
                rankedDocumentos.add(documento);
                pesos.add(entry.getValue());
            } else {
                rankedDocumentos.add(indexInsert, documento);
                pesos.add(indexInsert, entry.getValue());
            }
        }

        System.out.println("[Pesos] => " + pesos);

        // Genero respuesta json
        respuesta.setStateResponse(ResponseJson.State.OK);
        respuesta.setMessageResponse("Documentos encontrados exitosamente");

        long numRanking = 0;
        int pesoIndex = 0;
        JSONArray listaDocumentos = new JSONArray();
        for (Documento documento : rankedDocumentos) {
            numRanking++;
            if (numRanking > cantidadDocumentos) {
                break;
            }

            JSONObject documentoJson = new JSONObject();
            documentoJson.put("id", documento.getId());
            documentoJson.put("nombre", documento.getNombre());
            documentoJson.put("peso", pesos.get(pesoIndex));

            JSONObject documentoRankedJson = new JSONObject();
            documentoRankedJson.put("ranking", numRanking);
            documentoRankedJson.put("documento", documentoJson);

            listaDocumentos.put(documentoRankedJson);
            pesoIndex++;
        }

        System.out.println("[LISTA DOCUMENTOS] => " + listaDocumentos);
        System.out.println("/////////////////////////////////////////");
        System.out.println("/////////////////////////////////////////");
        System.out.println("/////////////////////////////////////////");
        System.out.println("/////////////////////////////////////////");
        System.out.println("/////////////////////////////////////////");
        System.out.println("/////////////////////////////////////////");
        System.out.println("/////////////////////////////////////////");

        respuesta.addDataResponse("documentos", listaDocumentos);
        return Response.ok(respuesta.getResponseJson()).build();
    }

    @GET
    @Path("/todos")
    @Produces("application/json")
    public Response buscarDocumentos() {
        List<Documento> documentos = daoDocumento.findAll();
        JSONArray listaDocumentos = new JSONArray();
        for (Documento documento : documentos) {
            JSONObject documentoJson = new JSONObject();
            documentoJson.put("id", documento.getId());
            documentoJson.put("nombre", documento.getNombre());
            documentoJson.put("url", documento.getUrl());
            listaDocumentos.put(documentoJson);

        }
        ResponseJson respuesta = new ResponseJson();
        respuesta.setStateResponse(ResponseJson.State.OK);
        respuesta.addDataResponse("documentos", listaDocumentos);
        return Response.ok(respuesta.getResponseJson()).build();
    }

}
