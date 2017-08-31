package com.serverless;

import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = Logger.getLogger(Handler.class);

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		String cityId = (String) input.get("id");
		LOG.info("cityId: " + cityId);
		String rest_api_response = null;
		String restURL = "http://api.openweathermap.org/data/2.5/weather?id="+ cityId +"&APPID=0c87cd4372ac1f3b58570b17e8f7326b&units=metric";
		LOG.info("restURL: " + restURL);

		try{
			rest_api_response = callRestAPI(restURL);

		} catch (Exception e) {
			LOG.error("Error while calling REST API" + e);
		}
		
		Response responseBody = new Response();

		JsonParser parser=new JsonParser();
		JsonObject object=(JsonObject)parser.parse(rest_api_response);

		String weather = object.get("weather").getAsJsonArray().get(0).getAsJsonObject().get("description").getAsString();
		String temperature = object.get("main").getAsJsonObject().get("temp").getAsString();
		String city = object.get("name").getAsString();
		String wind = object.get("wind").getAsJsonObject().get("speed").getAsString();
		wind = wind + " m/s";
		String pattern = "EEEE h:mm a";
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		String time = format.format(new Date());

		responseBody.setCity(city);
		responseBody.setTemperature(temperature);
		responseBody.setUpdatedTime(time);
		responseBody.setWeather(weather);
		responseBody.setWind(wind);

		Map<String, String> headers = new HashMap<>();
		headers.put("X-Powered-By", "AWS Lambda & Serverless");
		headers.put("Content-Type", "application/json");
		return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setObjectBody(responseBody)
				.setHeaders(headers)
				.build();
	}

	private static String callRestAPI(String restURL) throws Exception {
        URL obj = new URL(restURL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        int responseCode = con.getResponseCode();
        System.out.println("Response Code : " + responseCode);
        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine);
        }
        reader.close();

        return response+"";
    }
}
