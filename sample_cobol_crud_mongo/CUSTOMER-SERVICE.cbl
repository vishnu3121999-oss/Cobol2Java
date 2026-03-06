       IDENTIFICATION DIVISION.
       PROGRAM-ID. CUSTOMER-SERVICE.

       DATA DIVISION.
       WORKING-STORAGE SECTION.
       COPY "copybooks/CUSTOMER-REC.cpy".

       LINKAGE SECTION.
       COPY "copybooks/REQUEST-CTX.cpy".
       COPY "copybooks/RESPONSE-CTX.cpy".

       PROCEDURE DIVISION USING REQUEST-CTX RESPONSE-CTX.
       SERVICE-MAIN.
           EVALUATE REQ-OPERATION
               WHEN "CREATE"
                   PERFORM VALIDATE-CREATE
                   IF RESP-CODE = 0
                       CALL "CUSTOMER-REPOSITORY" USING REQUEST-CTX RESPONSE-CTX
                   END-IF
               WHEN "READ"
                   CALL "CUSTOMER-REPOSITORY" USING REQUEST-CTX RESPONSE-CTX
               WHEN "UPDATE"
                   PERFORM VALIDATE-UPDATE
                   IF RESP-CODE = 0
                       CALL "CUSTOMER-REPOSITORY" USING REQUEST-CTX RESPONSE-CTX
                   END-IF
               WHEN "DELETE"
                   CALL "CUSTOMER-REPOSITORY" USING REQUEST-CTX RESPONSE-CTX
               WHEN "LIST"
                   CALL "CUSTOMER-REPOSITORY" USING REQUEST-CTX RESPONSE-CTX
               WHEN OTHER
                   MOVE 400 TO RESP-CODE
                   MOVE "Invalid request operation" TO RESP-MESSAGE
           END-EVALUATE
           IF RESP-CODE = 0
               MOVE 200 TO RESP-CODE
           END-IF
           GOBACK.

       VALIDATE-CREATE.
           IF REQ-PAYLOAD = SPACES
               MOVE 422 TO RESP-CODE
               MOVE "Payload required for CREATE" TO RESP-MESSAGE
           END-IF.

       VALIDATE-UPDATE.
           IF REQ-CUST-ID = SPACES
               MOVE 422 TO RESP-CODE
               MOVE "Customer id required for UPDATE" TO RESP-MESSAGE
           END-IF.

