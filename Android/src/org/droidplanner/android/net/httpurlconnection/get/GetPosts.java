package org.droidplanner.android.net.httpurlconnection.get;

import android.support.annotation.NonNull;


import org.droidplanner.android.net.httpurlconnection.HttpRequest;
import org.droidplanner.android.net.httpurlconnection.meta.Header;
import org.droidplanner.android.net.httpurlconnection.meta.Url;
import org.droidplanner.android.net.model.Post;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetPosts extends HttpRequest<List<Post>> {

    public GetPosts() {
        super(Url.POST);
        addHeader(Header.Key.APPLICATION_ID, Header.Value.APPLICATION_ID);
        addHeader(Header.Key.SECRET_KEY, Header.Value.SECRET_KEY);
        addHeader(Header.Key.CONTENT_TYPE, Header.Value.CONTENT_TYPE);
        addHeader(Header.Key.APPLICATION_TYPE, Header.Value.APPLICATION_TYPE);
    }

    @NonNull
    @Override
    protected String httpMethod() {
        return GET;
    }

    @Override
    protected void response(String response, int statusCode) {
        if (callback != null) {
            notifySuccess(parseJSON(response), statusCode);
        }
    }

    private List<Post> parseJSON(String json) {
        List<Post> posts = new ArrayList<>();
        try {
            JSONObject jsonRoot = new JSONObject(json);
            JSONArray jsonData = jsonRoot.getJSONArray("data");
            int length = jsonData.length();
            for (int i = 0; i < length; i++) {
                JSONObject jsonPost = jsonData.getJSONObject(i);
                Post post = new Post();
                post.setObjectId(jsonPost.getString("objectId"));
                post.setUrl(jsonPost.getString("url"));
                posts.add(post);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return posts;
    }
}
