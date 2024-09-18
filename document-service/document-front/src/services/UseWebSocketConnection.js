import {Stomp} from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import {useEffect, useRef} from 'react';

const WS_URL = 'http://localhost:8080/ws'; // WebSocket 서버 주소 (SockJS 사용)

const UseWebSocketConnection = (documentId, onMessageReceived) => {
    const stompClientRef = useRef(null); // stompClient를 저장하는 ref

    useEffect(() => {
        const socket = new SockJS(WS_URL);
        const stompClient = Stomp.over(socket);
        stompClientRef.current = stompClient; // stompClient를 ref에 저장

        const connect = () => {
            stompClient.connect({}, () => {
                console.log('WebSocket 연결 성공');

                // 문서 전체 수신 처리
                stompClient.subscribe(`/topic/document/${documentId}`, (message) => {
                    const receivedData = JSON.parse(message.body);
                    console.log("Received full document from topic:", receivedData.content);
                    onMessageReceived(receivedData.content);  // 수신한 전체 문서 처리
                });
            }, (error) => {
                console.error('WebSocket 연결 실패:', error);
                setTimeout(connect, 5000); // 5초 후 재연결 시도
            });
        };

        connect();

        // 컴포넌트 언마운트 시 연결 해제
        return () => {
            if (stompClientRef.current) {
                stompClientRef.current.disconnect();
                console.log('WebSocket 연결 해제');
            }
        };
    }, [documentId, onMessageReceived]);

    // 전체 문서를 서버로 전송하는 함수
    const sendDocument = (content) => {
        if (stompClientRef.current && stompClientRef.current.connected) {
            console.log("Sending full document via WebSocket:", content);
            stompClientRef.current.send(`/app/editDocument`, {}, JSON.stringify({
                documentId: documentId,
                content: content
            }));
        } else {
            console.error("WebSocket is not connected");
        }
    };

    return {sendDocument};
};

export default UseWebSocketConnection;