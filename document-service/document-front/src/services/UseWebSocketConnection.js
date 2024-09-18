import {Stomp} from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import {useEffect, useRef} from 'react';

const WS_URL = 'http://localhost:8080/ws'; // WebSocket 서버 주소 (SockJS 사용)

const UseWebSocketConnection = (documentId, onMessageReceived, chunkSize = 4096) => {
    const stompClientRef = useRef(null); // stompClient를 저장하는 ref

    useEffect(() => {
        const socket = new SockJS(WS_URL);
        const stompClient = Stomp.over(socket);
        stompClientRef.current = stompClient; // stompClient를 ref에 저장

        const connect = () => {
            stompClient.connect({}, () => {
                console.log('WebSocket 연결 성공');

                // 문서 청크 수신 처리
                stompClient.subscribe(`/topic/document/${documentId}`, (message) => {
                    const receivedData = JSON.parse(message.body);
                    console.log("Received chunk from topic:", receivedData.chunk);

                    // 청크와 마지막 청크인지 여부를 onMessageReceived에 전달
                    onMessageReceived(receivedData.chunk, receivedData.isLastChunk);
                });
            }, (error) => {
                console.error('WebSocket 연결 실패:', error);
                // 연결이 실패할 경우 재시도
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

    // 데이터를 청크로 분할하여 전송하는 함수
    const sendDocumentChunks = (content) => {
        if (stompClientRef.current && stompClientRef.current.connected) {
            const totalChunks = Math.ceil(content.length / chunkSize);

            for (let i = 0; i < totalChunks; i++) {
                const chunk = content.slice(i * chunkSize, (i + 1) * chunkSize);
                const isLastChunk = (i === totalChunks - 1);

                console.log(`Sending chunk: ${i + 1}/${totalChunks}, chunk size: ${chunk.length}, isLastChunk: ${isLastChunk}`);
                stompClientRef.current.send(`/app/editDocument`, {}, JSON.stringify({
                    documentId: documentId,
                    chunk: chunk,
                    isLastChunk: isLastChunk
                }));
            }
        } else {
            console.error("WebSocket is not connected");
        }
    };

    return {sendDocumentChunks};
};

export default UseWebSocketConnection;