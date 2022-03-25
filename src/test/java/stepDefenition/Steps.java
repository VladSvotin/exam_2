package stepDefenition;

import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.Затем;
import io.cucumber.java.ru.И;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static Utils.Configuration.getConfigurationValue;
import static io.restassured.RestAssured.given;

public class Steps {

    public String lastEp;
    public String mortySpec;
    public String mortyLoc;
    public String lastChar;
    public String lastCharSpec;
    public String lastCharLoc;

    @Дано("^Получение информации по персонажу")
    public void getMortyInfomation(String id) {

        Response response1 = given()
                .baseUri(getConfigurationValue("Url"))
                .contentType(ContentType.JSON)
                .when()
                .get("character/" + id)
                .then()
                .extract().response();

        String mortyInfo = response1.getBody().asString();
        JSONObject jsonMorty = new JSONObject(mortyInfo);
        JSONArray epWithMorty = jsonMorty.getJSONArray("episode");
        int epCount = epWithMorty.length();
        lastEp = epWithMorty.getString(epCount - 1);
        mortySpec = jsonMorty.getString("species");
        mortyLoc = jsonMorty.getJSONObject("location").getString("name");
    }

    @Затем("^Последний персонаж в эпизоде$")
    public void getLastChar() {
        Response response2 = given()
                .baseUri(getConfigurationValue("Url"))
                .contentType(ContentType.JSON)
                .when()
                .get(lastEp)
                .then()
                .extract().response();

        String lastMortyEp = response2.getBody().asString();
        JSONObject jsonLastEp = new JSONObject(lastMortyEp);
        JSONArray charactersInLastEp = jsonLastEp.getJSONArray("characters");
        int charCount = charactersInLastEp.length();
        lastChar = charactersInLastEp.getString(charCount - 1);
    }

    @Затем("^Информация по последнему персонажу в эпизоде$")
    public void getLastCharInformation() {
        Response response3 = given()
                .baseUri(getConfigurationValue("Url"))
                .contentType(ContentType.JSON)
                .when()
                .get(lastChar)
                .then()
                .extract().response();

        String desiredCharacter = response3.getBody().asString();
        JSONObject jsonCharacter = new JSONObject(desiredCharacter);
        lastCharSpec = jsonCharacter.getString("species");
        lastCharLoc = jsonCharacter.getJSONObject("location").getString("name");
    }

    @И("^Проверка на совпадение рас$")
    public void specAssert() {
        Assertions.assertEquals(mortySpec, lastCharSpec, "Совпадает");
    }

    @И("^Проверка на не совпадение локаций$")
    public void locAssert() {
        Assertions.assertNotEquals(mortyLoc, lastCharLoc, "Не совпадает");
    }

    @Затем("^Запрос на регрес и проверка итоговых результатов$")
    public void test1() throws IOException {
        JSONObject requestBody = new JSONObject(new String(Files.readAllBytes(Paths.get("src/test/resources/json/test1.json"))));
        requestBody.put("name", "Tomato");
        requestBody.put("job", "Eat maket");

        Response response3 = given()
                .baseUri(getConfigurationValue("regres"))
                .contentType("application/json;charset=UTF-8")
                .log().all()
                .when()
                .body(requestBody.toString())
                .post("users")
                .then()
                .statusCode(201)
                .log().all()
                .extract().response();

        String userTomato = response3.getBody().asString();
        JSONObject json = new JSONObject(userTomato);
        Assertions.assertEquals(json.getString("name"), "Tomato");
        Assertions.assertEquals(json.getString("job"), "Eat maket");
    }

}
