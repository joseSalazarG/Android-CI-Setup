package steps;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Objects;

// TODO: Esta clase guarda plantillas de metodos para realizar peticiones a una API.
//  Se debe modificar el contenido de los metodos para que se ajusten a la API que se va a testear.
//  Luego basta con hacer una instancia de esta clase en el logic que se necesite y llamar a los metodos.

@Log4j2
public class ApiHooks {

    //API Endpoints
    public static final String urlToken = "https://xxxxxxx/hubapi/login";
    public static final String credenciales = "{\"email\":\"xxxxx\",\"password\":\"xxxxxxx\"}";
    public static final String authToken = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
    public static String hubToken = null;

    /**
     * Obtiene el token de autenticación para realizar solicitudes en la aplicación.
     * @param baseUrl El endpoint a la api para obtener el token.
     * @param jsonCredenciales las credenciales para conectarse.
     * @param authorization token de autorizacion para acceder a la api.
     * @return el token de autenticación.
     */
    public String getHubToken(String baseUrl, String jsonCredenciales ,String authorization ) {

        RequestSpecification request = RestAssured.given();
        request.header("Authorization", authorization);
        request.contentType(ContentType.JSON);
        request.body(jsonCredenciales);

        Response response = request.post(baseUrl);
        int statusCode = response.getStatusCode();
        String cachedToken = null;

        if (statusCode == 200) {
            cachedToken = response.jsonPath().getString("data.token");
            System.err.println(cachedToken);
        }
        return cachedToken;
    }
    /**
     * Busca la ultima solicitud en la aplicación segun la id del usuario  y devuelve su identificador.
     * @param user el numero de id del usuario.
     * @return El identificador del usuario.
     * @throws IllegalStateException Si no se encuentran usuarios en la respuesta.
     */
    public String searchRequestId(String user) {
        String url = "https://xxxxxxxxxxx/hubapi/members?search=";
        String searchUrl = url + user;

        hubToken = getHubToken(urlToken,credenciales, authToken);

        RequestSpecification request = RestAssured.given();
        request.header("Authorization", hubToken);
        request.contentType(ContentType.JSON);

        Response response = request.get(searchUrl);
        List<String> userIds = response.jsonPath().getList("data.data._id");

        if (!userIds.isEmpty()) {

            return userIds.get(0);
        } else {
            throw new IllegalStateException("No se encontraron solicitudes en la respuesta.");
        }
    }


    /**
     * Busca la ultima solicitud en la aplicación segun la cedula del usuario  y devuelve su tipo.
     * @param user el numero de cedula del usuario.
     * @return El tipo de solicitud.
     * @throws IllegalStateException Si no se encuentra el tipo de las solicitudes en la respuesta.
     */
    public String searchRequestChangeType(String user) {
        String url = "https://xxxxxxxxxxx/api/dev/userstreamchange/list_get?search=";
        String baseUrl = url + user;

        hubToken = getHubToken(urlToken,credenciales, authToken);

        RequestSpecification request = RestAssured.given();
        request.header("Authorization", hubToken);
        request.contentType(ContentType.JSON);

        Response response = request.get(baseUrl);
        List<String> solicitudChangeTypes = response.jsonPath().getList("data.data.changeType");

        if (!solicitudChangeTypes.isEmpty()) {

            return solicitudChangeTypes.get(0);
        } else {
            throw new IllegalStateException("No se encontraron solicitudes en la respuesta.");
        }
    }

    /**
     * Cambia el estado de una solicitud en la aplicación según la cedula del usuario y su tipo.
     * @param user el número de cedula del usuario.
     * @param status el estado al cual se quiere cambiar la solicitud nueva.
     * @throws IllegalStateException Si no se encuentra el tipo de las solicitudes en la respuesta.
     */
    public void putChangeStatus(String user, String status) {
        String baseUrl = "https://xxxxxxxxxxx/api/dev/userstreamchange/status";
        String id = searchRequestId(user);
        String changeType = searchRequestChangeType(user);
        String json= null;

        if(Objects.equals(changeType, "NEW")){
            json = "{ \"_id\": \"" + id + "\",\"status\": \"" + status + "\",\"statusReason\": \"Motivo de prueba\" }";

        } else if (Objects.equals(changeType, "DELETE")) {
            json = "{ \"_id\": \"" + id + "\",\"status\": \"ACEPTADO\",\"statusReason\": \"Motivo de prueba\" }";
        }

        hubToken = getHubToken(urlToken,credenciales, authToken);

        RequestSpecification request = RestAssured.given();
        request.header("Authorization", hubToken);
        request.contentType(ContentType.JSON);
        request.body(json);

        Response response = request.put(baseUrl);
        int statusCode = response.getStatusCode();


        if (statusCode == 200) {
            String cachedToken = response.jsonPath().getString("data");
        } else {
            throw new IllegalStateException("No se encontraron solicitudes que modificar en la respuesta.");
        }
    }

    //Busca un reporte para su posterior eliminacion
    public String searchReporte(String idreporte) {
        String url = "https://xxxxxxxxxxx/api/dev/report/list?search=";
        String baseUrl = url + idreporte;

        hubToken = getHubToken(urlToken,credenciales, authToken);

        RequestSpecification request = RestAssured.given();
        request.header("Authorization", hubToken);
        request.contentType(ContentType.JSON);

        Response response = request.get(baseUrl);
        List<String> solicitudId = response.jsonPath().getList("data.data._id");

        if (!solicitudId.isEmpty()) {
            return solicitudId.get(0);
        } else {
            throw new IllegalStateException("No se encontro la id del reporte");
        }
    }

    //Elimino el reporte
    public void eliminarReporteHub(String idReporte) {
        String baseUrl = "https://xxxxxxxxxxx/api/dev/report/";
        String id = null;
        String req = null;

        id = searchReporte(idReporte);
        req = baseUrl + id + "/remove";

        hubToken = getHubToken(urlToken,credenciales, authToken);

        RequestSpecification request = RestAssured.given();
        request.header("Authorization", hubToken);

        Response response = request.delete(req);
        int statusCode = response.getStatusCode();

        if (statusCode == 200) {
            log.info("Fue eliminado el reporte");
        } else {
            throw new IllegalStateException("No se pudo aprobar eliminar el reporte");
        }
    }
}