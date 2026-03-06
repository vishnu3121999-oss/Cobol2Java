       IDENTIFICATION DIVISION.
       PROGRAM-ID. CUSTOMER-API.

       DATA DIVISION.
       WORKING-STORAGE SECTION.
       COPY "copybooks/REQUEST-CTX.cpy".
       COPY "copybooks/RESPONSE-CTX.cpy".

       PROCEDURE DIVISION.
       MAIN-ENTRY.
           PERFORM INIT-MOCK-REQUEST
           EVALUATE REQ-OPERATION
               WHEN "CREATE"
                   CALL "CUSTOMER-SERVICE" USING REQUEST-CTX RESPONSE-CTX
               WHEN "READ"
                   CALL "CUSTOMER-SERVICE" USING REQUEST-CTX RESPONSE-CTX
               WHEN "UPDATE"
                   CALL "CUSTOMER-SERVICE" USING REQUEST-CTX RESPONSE-CTX
               WHEN "DELETE"
                   CALL "CUSTOMER-SERVICE" USING REQUEST-CTX RESPONSE-CTX
               WHEN "LIST"
                   CALL "CUSTOMER-SERVICE" USING REQUEST-CTX RESPONSE-CTX
               WHEN OTHER
                   MOVE 400 TO RESP-CODE
                   MOVE "Unsupported operation" TO RESP-MESSAGE
           END-EVALUATE
           PERFORM LOG-RESPONSE
           GOBACK.

       INIT-MOCK-REQUEST.
           MOVE "CREATE" TO REQ-OPERATION
           MOVE "67f5b3f9e7c4a4f3dd918001" TO REQ-CUST-ID
           MOVE "{""name"":""Ava Stone"",""email"":""ava@corp.com"",""status"":""ACTIVE""}" TO REQ-PAYLOAD
           MOVE "corr-20260306-001" TO REQ-CORRELATION-ID.

       LOG-RESPONSE.
           DISPLAY "RESP-CODE=" RESP-CODE
           DISPLAY "RESP-MESSAGE=" RESP-MESSAGE
           DISPLAY "RESP-BODY=" RESP-BODY.

