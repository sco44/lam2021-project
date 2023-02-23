package it.unibo.cs.lam2021.api;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;

import com.google.common.util.concurrent.ListenableFuture;

import it.unibo.cs.lam2021.preferences.EncryptedPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiService {

    private static final String BASE_URL = "https://lam21.iot-prism-lab.cs.unibo.it";

    private static volatile ApiService INSTANCE;

    private final ApiInterface service;
    private String authorization;

    public static class AlreadyRegisteredException extends Exception {
        public AlreadyRegisteredException() {
            super("AlreadyRegisteredException");
        }
    }

    public static class InvalidCredentialsException extends Exception {
        public InvalidCredentialsException() {
            super("InvalidCredentialsException");
        }
    }

    public static class InvalidOwnerException extends Exception {
        public InvalidOwnerException() {
            super("InvalidOwnerException");
        }
    }

    private static <T> ListenableFuture<T> getDefaultCallbackFuture(Call<T> call, int successCode) {
        return CallbackToFutureAdapter.getFuture(completer -> {
            call.enqueue(new Callback<T>() {
                @Override
                public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                    if (response.code() == successCode)
                        completer.set(response.body());
                    else
                        completer.setException(new HttpException(response));
                }

                @Override
                public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                    completer.setException(t);
                }
            });

            return "ApiService.DefaultCallback";
        });
    }

    private ApiService(String token) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(ApiInterface.class);

        if(token != null)
            setAuthorization(token);
    }

    public static ApiService getInstance(Context ctx) {
        if(INSTANCE == null) {
            synchronized (ApiService.class) {
                if (INSTANCE == null)
                    INSTANCE = new ApiService(EncryptedPreferences.getInstance(ctx).getAuthorizationToken());
            }
        }

        return INSTANCE;
    }

    public void setAuthorization(String token) {
        authorization = "Bearer " + token;
    }

    public ListenableFuture<User> register(String email, String username, String password) {
        User user = new User(email, username, password);

        Call<User> call = service.register(user);
        return CallbackToFutureAdapter.getFuture(completer -> {
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                    if(response.code() == 201)
                        completer.set(response.body());
                    else if(response.code() == 500)
                        completer.setException(new AlreadyRegisteredException());
                    else
                        completer.setException(new HttpException(response));
                }

                @Override
                public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                    completer.setException(t);
                }
            });

            return "ApiService.Register";
        });
    }

    public ListenableFuture<User> login(String email, String password) {
        User user = new User(email, password);
        Call<Token> call = service.login(user);
        return CallbackToFutureAdapter.getFuture(completer -> {
            call.enqueue(new Callback<Token>() {
                @Override
                public void onResponse(@NonNull Call<Token> call, @NonNull Response<Token> response) {
                    if(response.code() == 201) {
                        String authToken = response.body().getAccessToken();
                        setAuthorization(authToken);
                        Call<User> userDetailsCall = service.getUserDetails(authorization);
                        userDetailsCall.enqueue(new Callback<User>() {
                            @Override
                            public void onResponse(@NonNull Call<User> userCall, @NonNull Response<User> userResponse) {
                                if(userResponse.code() == 200) {
                                    userResponse.body().setAuthorizationToken(authToken);
                                    completer.set(userResponse.body());
                                } else
                                    completer.setException(new HttpException(userResponse));
                            }

                            @Override
                            public void onFailure(@NonNull Call<User> userCall, @NonNull Throwable t) {
                                completer.setException(t);
                            }
                        });
                    } else if(response.code() == 401)
                        completer.setException(new InvalidCredentialsException());
                    else
                        completer.setException(new HttpException(response));
                }

                @Override
                public void onFailure(@NonNull Call<Token> call, @NonNull Throwable t) {
                    completer.setException(t);
                }
            });

            return "ApiService.Login";
        });
    }

    public ListenableFuture<User> getUserDetails() {
        Call<User> call = service.getUserDetails(authorization);
        return getDefaultCallbackFuture(call, 200);
    }

    public ListenableFuture<Products> getProductsByBarcode(String barcode) {
        Call<Products> call = service.getProductsByBarcode(authorization, barcode);
        return getDefaultCallbackFuture(call, 200);
    }

    public ListenableFuture<Void> deleteProduct(String id) {
        Call<Product> call = service.deleteProduct(authorization, id);
        return CallbackToFutureAdapter.getFuture(completer -> {
            call.enqueue(new Callback<Product>() {
                @Override
                public void onResponse(@NonNull Call<Product> call, @NonNull Response<Product> response) {
                    if(response.code() == 200 && id.equals(response.body().getId()))
                        completer.set(null);
                    else if(response.code() == 403)
                        completer.setException(new InvalidOwnerException());
                    else
                        completer.setException(new HttpException(response));
                }

                @Override
                public void onFailure(@NonNull Call<Product> call, @NonNull Throwable t) {
                    completer.setException(t);
                }
            });

            return "ApiService.DeleteProduct";
        });
    }

    public ListenableFuture<Product> postProductDetails(String name, String description, String barcode, String token) {
        Product p = new Product(name, description, barcode, token);
        Call<Product> call = service.postProductDetails(authorization, p);
        return getDefaultCallbackFuture(call, 201);
    }

    public ListenableFuture<ProductPreference> postProductPreference(String productId, int rating, String token) {
        ProductPreference pref = new ProductPreference(productId, rating, token);
        Call<ProductPreference> call = service.postProductPreference(authorization, pref);
        return getDefaultCallbackFuture(call, 201);
    }
}
