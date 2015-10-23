package com.brodev.socialapp.entity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <code>ReCaptcha</code> extends {@link android.widget.ImageView} to let you embed a <a href="http://captcha.net/">CAPTCHA</a>
 * in your applications in order to protect them against spam and other types of automated abuse.
 *
 * @see <a href="https://developers.google.com/recaptcha/">reCAPTCHA</a>
 */
public class ReCaptcha extends ImageView {
    /**
     * Listener that is called when an attempt to show a <a href="http://captcha.net/">CAPTCHA</a>
     * is completed.
     */
    public interface OnShowChallengeListener {
        /**
         * Called when an attempt to show a <a href="http://captcha.net/">CAPTCHA</a> is completed.
         *
         * @param shown <code>true</code> if a <a href="http://captcha.net/">CAPTCHA</a> is shown;
         *              otherwise, <code>false</code>.
         */
        void onChallengeShown(boolean shown);
    }

    /**
     * Listener that is called when an answer entered by the user to solve the <a href="http://captcha.net/">CAPTCHA</a>
     * displayed is verified.
     */
    public interface OnVerifyAnswerListener {
        /**
         * Called when an answer entered by the user to solve the <a href="http://captcha.net/">CAPTCHA</a>
         * displayed is verified.
         *
         * @param success <code>true</code> if the <a href="http://captcha.net/">CAPTCHA</a> is solved successfully;
         *                otherwise, <code>false</code>.
         */
        void onAnswerVerified(boolean success);
    }

    private static final String TAG = "ReCaptcha";

    private static final String VERIFICATION_URL = "http://www.google.com/recaptcha/api/verify";

    private static final String CHALLENGE_URL = "http://www.google.com/recaptcha/api/challenge?k=%s";
    private static final String RECAPTCHA_OBJECT_TOKEN_URL = "http://www.google.com/recaptcha/api/reload?c=%s&k=%s&type=%s";
    private static final String IMAGE_URL = "http://www.google.com/recaptcha/api/image?c=%s";

    private String imageToken;
    private String globalChallenge;
    private HashMap<String, String> publicKeyChallengeMap = new HashMap<String, String>();

    public ReCaptcha(final Context context) {
        super(context);
    }

    public ReCaptcha(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public ReCaptcha(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Returns a new instance of {@link org.apache.http.client.HttpClient} for downloading ReCaptcha images.
     * <p>Subclasses may override this method and return customized {@link org.apache.http.client.HttpClient},
     * such as an {@link android.net.http.AndroidHttpClient} with custom {@link org.apache.http.params.HttpParams}.</p>
     * <p>The default behavior returns a {@link org.apache.http.impl.client.DefaultHttpClient}</p>
     *
     * @return a {@link org.apache.http.client.HttpClient} for downloading ReCaptcha images.
     */
    protected HttpClient createHttpClient() {
        return new DefaultHttpClient();
    }

    public final boolean showChallenge(final String publicKey) throws ReCaptchaException, IOException {
        if (TextUtils.isEmpty(publicKey)) {
            throw new IllegalArgumentException("publicKey cannot be null or empty");
        }

        this.setImageDrawable(null);

        this.imageToken = null;

        final Bitmap bitmap = this.downloadImage(publicKey);

        this.setImageBitmap(bitmap);

        return bitmap != null;
    }

    /**
     * Downloads and shows a <a href="http://captcha.net/">CAPTCHA</a> image asynchronously.
     * <p>This method executes asynchronously and can be invoked from the UI thread.
     * For synchronous invocation, use {@link #showChallenge(String)}.</p>
     *
     * @param publicKey The public key that is unique to your domain and sub-domains (unless it is global key).
     * @param listener  The callback to call when an attempt to show a <a href="http://captcha.net/">CAPTCHA</a> is completed.
     * @see #showChallenge(String)
     */
    public final void showChallengeAsync(final String publicKey, final OnShowChallengeListener listener) {
        if (TextUtils.isEmpty(publicKey)) {
            throw new IllegalArgumentException("publicKey cannot be null or empty");
        }

        this.setImageDrawable(null);

        this.imageToken = null;

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(final Message message) {
                final Bitmap bitmap = (Bitmap) message.obj;
                final Bitmap scaled = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() * 2, bitmap.getHeight() * 2, true);

                bitmap.recycle();

                ReCaptcha.this.setImageBitmap(scaled);

                if (listener != null) {
                    listener.onChallengeShown(message.obj != null);
                }
            }
        };

        new AsyncTask<String, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(final String... publicKeys) {
                try {
                    return ReCaptcha.this.downloadImage(publicKeys[0]);
                } catch (ReCaptchaException e) {
                    Log.e(ReCaptcha.TAG, "The downloaded CAPTCHA content is malformed", e);
                } catch (final IOException e) {
                    Log.e(ReCaptcha.TAG, "A protocol or network connection problem has occurred", e);
                }

                return null;
            }

            @Override
            protected void onPostExecute(final Bitmap bitmap) {
                handler.sendMessage(handler.obtainMessage(0, bitmap));
            }
        }.execute(publicKey);
    }

    /**
     * Checks whether the answer entered by the user is correct after your application is successfully
     * displaying <a href="https://developers.google.com/recaptcha/">reCAPTCHA</a>.
     * <p>This method executes synchronously and should not be invoked from the UI thread.
     * For asynchronous invocation, you may use {@link android.os.AsyncTask} or call {@link #verifyAnswerAsync(String, String, ReCaptcha.OnVerifyAnswerListener)} instead.</p>
     *
     * @param privateKey The private key that is unique to your domain and sub-domains (unless it is a global key).
     * @param answer     The string the user entered to solve the <a href="http://captcha.net/">CAPTCHA</a> displayed.
     * @return <code>true</code> if the <a href="https://developers.google.com/recaptcha/">reCAPTCHA</a> was successful;
     * otherwise <code>false</code>.
     * @throws java.io.IOException in case of a protocol or network connection problem.
     * @see #verifyAnswerAsync(String, String, ReCaptcha.OnVerifyAnswerListener)
     */
    public final boolean verifyAnswer(final String privateKey, final String answer) throws IOException {
        if (TextUtils.isEmpty(privateKey)) {
            throw new IllegalArgumentException("privateKey cannot be null or empty");
        }

        if (TextUtils.isEmpty(answer)) {
            throw new IllegalArgumentException("answer cannot be null or empty");
        }

        return this.submitAnswer(privateKey, answer);
    }

    /**
     * Checks asynchronously whether the answer entered by the user is correct after your application
     * is successfully displaying <a href="https://developers.google.com/recaptcha/">reCAPTCHA</a>.
     *
     * @param privateKey The private key that is unique to your domain and sub-domains (unless it is a global key).
     * @param answer     The string the user entered to solve the <a href="http://captcha.net/">CAPTCHA</a> displayed.
     * @param listener   The callback to call when an answer entered by the user is verified.
     * @see #verifyAnswer(String, String)
     */
    public final void verifyAnswerAsync(final String privateKey, final String answer, final OnVerifyAnswerListener listener) {
        if (TextUtils.isEmpty(privateKey)) {
            throw new IllegalArgumentException("privateKey cannot be null or empty");
        }

        if (TextUtils.isEmpty(answer)) {
            throw new IllegalArgumentException("answer cannot be null or empty");
        }

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(final Message message) {
                if (listener != null) {
                    listener.onAnswerVerified((Boolean) message.obj);
                }
            }
        };

        new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(final String... params) {
                try {
                    return ReCaptcha.this.submitAnswer(params[0], params[1]);
                } catch (final IOException e) {
                    Log.e(ReCaptcha.TAG, "A protocol or network connection problem has occurred", e);
                }

                return Boolean.FALSE;
            }

            @Override
            protected void onPostExecute(final Boolean result) {
                handler.sendMessage(handler.obtainMessage(0, result));
            }
        }.execute(privateKey, answer);
    }

    public String getChallenge() {
        return globalChallenge;
    }

    private Bitmap downloadImage(final String publicKey) throws ReCaptchaException, IOException {
        final HttpClient httpClient = this.createHttpClient();

        try {
            String challenge = getChallenge(publicKey);

            if (challenge == null) {
                throw new ReCaptchaException("ReCaptcha challenge not found");
            }

            String imageToken = getImageToken(challenge, publicKey);
            globalChallenge = imageToken;
            if (imageToken == null) {
                throw new ReCaptchaException("Image token not found");
            }
            this.imageToken = imageToken;     
            final String imageUrl = String.format(ReCaptcha.IMAGE_URL, imageToken);

            final HttpResponse response = httpClient.execute(new HttpGet(imageUrl));

            try {
                final Bitmap bitmap = BitmapFactory.decodeStream(response.getEntity().getContent());

                if (bitmap == null) {
                    throw new ReCaptchaException("Invalid CAPTCHA image");
                }

                return bitmap;
            } finally {
                if (response.getEntity() != null) {
                    response.getEntity().consumeContent();
                }
            }
        } catch (JSONException e) {
            throw new ReCaptchaException("Unable to parse challenge response");
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
//        return null;
    }

    private String getImageToken(String challenge, String publicKey) throws IOException {
        HttpClient httpClient = this.createHttpClient();
        String imageTokenUrl = String.format(ReCaptcha.RECAPTCHA_OBJECT_TOKEN_URL, challenge, publicKey, "image");
        String imageTokenResponse = httpClient.execute(new HttpGet(imageTokenUrl), new BasicResponseHandler());
        return substringBetween(imageTokenResponse, "('", "',");

    }

    private String getChallenge(String publicKey) throws IOException, JSONException {
            HttpClient httpClient = this.createHttpClient();
            String challenegeResponse = httpClient.execute(new HttpGet(String.format(ReCaptcha.CHALLENGE_URL, publicKey)), new BasicResponseHandler());
            final String recaptchaStateString = substringBetween(
                    challenegeResponse
                    , "RecaptchaState = ", "}") + "}";

            JSONObject recaptchaStateObject = new JSONObject(recaptchaStateString);
            String challenge = recaptchaStateObject.getString("challenge");
            publicKeyChallengeMap.put(publicKey, challenge);
            return challenge;
    }


    private boolean submitAnswer(final String privateKey, final String answer) throws IOException {
        final HttpClient httpClient = this.createHttpClient();
        final HttpPost request = new HttpPost(ReCaptcha.VERIFICATION_URL);
        final List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("privatekey", privateKey));
        params.add(new BasicNameValuePair("remoteip", "127.0.0.1"));
        params.add(new BasicNameValuePair("challenge", this.imageToken));
        params.add(new BasicNameValuePair("response", answer));

        try {
            request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

            return httpClient.execute(request, new BasicResponseHandler()).startsWith("true");
        } catch (final UnsupportedEncodingException e) {
            Log.e(ReCaptcha.TAG, "UTF-8 encoding is not supported by this platform", e);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }

        return false;
    }

    private static String substringBetween(String str, String open, String close) {
        if (str == null || open == null || close == null) {
            return null;
        }
        int start = str.indexOf(open);
        if (start != -1) {
            int end = str.indexOf(close, start + open.length());
            if (end != -1) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }
}
