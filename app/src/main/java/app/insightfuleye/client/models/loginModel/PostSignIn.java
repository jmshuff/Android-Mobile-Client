package app.insightfuleye.client.models.loginModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PostSignIn {

        @SerializedName("email")
        @Expose
        private String email;
        @SerializedName("password")
        @Expose
        private String password;
        @SerializedName("refresh_token")
        @Expose
        private String refreshToken;

        /**
         * No args constructor for use in serialization
         *
         */
        public PostSignIn() {
        }

        /**
         *
         * @param password
         * @param email
         */
        public PostSignIn(String email, String password) {
            super();
            this.email = email;
            this.password = password;
        }

        public PostSignIn(String refreshToken){
            super();
            this.refreshToken=refreshToken;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
