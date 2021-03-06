package ko2ic.dagger2.infrastructure.repository;

import android.content.Context;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import ko2ic.dagger2.domain.model.Weather;
import ko2ic.dagger2.infrastructure.repository.event.common.AbstractHttpStatusErrorEvent;
import ko2ic.dagger2.infrastructure.repository.event.common.RuntimeExceptionEvent;

public class WeatherRepository {

    private Context mContext;

    @Inject
    public WeatherRepository(Context context) {
        mContext = context;
    }

    public void fetchWeather(String cityCode) {
        String url = "http://weather.livedoor.com/forecast/webservice/json/v1?city=" + cityCode;

        RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.add(new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            Weather entity = new Weather();

                            String title = response.getString("title");

                            entity.title = title;
                            EventBus.getDefault().post(new WeatherEventSuccess(entity));
                        } catch (JSONException e) {
                            EventBus.getDefault().post(new RuntimeExceptionEvent(e));
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse response = error.networkResponse;
                        if (response == null) {
                            EventBus.getDefault().post(new WeatherEventFailure(404));
                            return;
                        }
                        EventBus.getDefault().post(new WeatherEventFailure(response.statusCode));
                    }
                }));


    }

    public class WeatherEventSuccess {

        private Weather mWeather;

        public WeatherEventSuccess(Weather weather) {
            this.mWeather = weather;
        }

        public Weather getWeather() {
            return mWeather;
        }
    }

    public class WeatherEventFailure extends AbstractHttpStatusErrorEvent {

        public WeatherEventFailure(int statusCode) {
            super(statusCode);
        }
    }
}
