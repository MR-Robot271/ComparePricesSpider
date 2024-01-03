package com.kb.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

//@Component
public class MyHandler extends TextWebSocketHandler {

    private static WebSocketSession session;

    /**
     * @Description: 获取连接后的session
     * @Param: [session]
     * @return: void
     * @Date: 2023/12/8
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        this.session = session;
    }
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 这里处理 WebSocket 消息
    }

    // 方法用于向前端发送数据 只有西域的数据
    public void sendDataToClient(int xiyuCrawledNum, int xiyuTotalNum) throws IOException {
        String message = "{\"crawledNum\": " + xiyuCrawledNum + ", \"totalNum\": " + xiyuTotalNum + "}";
        session.sendMessage(new TextMessage(message));
    }

    // 方法用于向前端发送数据 有西域和震坤行的数据
    public void sendDataToClient(int xiyuCrawledNum, int xiyuTotalNum, int zkhCrawledNum, int zkhTotalNum) throws IOException {
        String message = "{\"xiyuCrawledNum\": " + xiyuCrawledNum + ", \"xiyuTotalNum\": " + xiyuTotalNum + ", \"zkhCrawledNum\": " + zkhCrawledNum + ", \"zkhTotalNum\": " + zkhTotalNum+ "}";
        session.sendMessage(new TextMessage(message));
    }
}