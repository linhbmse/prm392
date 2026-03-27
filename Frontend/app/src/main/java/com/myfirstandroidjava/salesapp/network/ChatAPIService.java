package com.myfirstandroidjava.salesapp.network;

import com.myfirstandroidjava.salesapp.models.ChatHistoryResponse;
import com.myfirstandroidjava.salesapp.models.ChatMessage;
import com.myfirstandroidjava.salesapp.models.ConversationItem;
import com.myfirstandroidjava.salesapp.models.SendMessageRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ChatAPIService {
    @GET("Chat/conversations")
    Call<List<ConversationItem>> getConversations();

    @GET("Chat/messages")
    Call<ChatHistoryResponse> getChatHistory(
            @Query("otherUserId") Integer otherUserId,
            @Query("skip") int skip,
            @Query("take") int take
    );

    @POST("Chat/messages")
    Call<ChatMessage> sendMessage(@Body SendMessageRequest request);
}
