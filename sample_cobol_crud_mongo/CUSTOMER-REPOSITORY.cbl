       IDENTIFICATION DIVISION.
       PROGRAM-ID. CUSTOMER-REPOSITORY.

       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  DB-NAME               PIC X(20) VALUE "crm_prod".
       01  COLL-NAME             PIC X(20) VALUE "customers".

       LINKAGE SECTION.
       COPY "copybooks/REQUEST-CTX.cpy".
       COPY "copybooks/RESPONSE-CTX.cpy".

       PROCEDURE DIVISION USING REQUEST-CTX RESPONSE-CTX.
       REPO-MAIN.
           EVALUATE REQ-OPERATION
               WHEN "CREATE"
                   CALL "MONGO-INSERT" USING DB-NAME COLL-NAME REQ-PAYLOAD
                   MOVE 201 TO RESP-CODE
                   MOVE "Created" TO RESP-MESSAGE
                   MOVE "{""result"":""created""}" TO RESP-BODY
               WHEN "READ"
                   CALL "MONGO-FIND-ONE" USING DB-NAME COLL-NAME REQ-CUST-ID RESP-BODY
                   MOVE 200 TO RESP-CODE
                   MOVE "Read success" TO RESP-MESSAGE
               WHEN "UPDATE"
                   CALL "MONGO-UPDATE" USING DB-NAME COLL-NAME REQ-CUST-ID REQ-PAYLOAD
                   MOVE 200 TO RESP-CODE
                   MOVE "Updated" TO RESP-MESSAGE
                   MOVE "{""result"":""updated""}" TO RESP-BODY
               WHEN "DELETE"
                   CALL "MONGO-DELETE" USING DB-NAME COLL-NAME REQ-CUST-ID
                   MOVE 204 TO RESP-CODE
                   MOVE "Deleted" TO RESP-MESSAGE
                   MOVE SPACES TO RESP-BODY
               WHEN "LIST"
                   CALL "MONGO-FIND-MANY" USING DB-NAME COLL-NAME RESP-BODY
                   MOVE 200 TO RESP-CODE
                   MOVE "List success" TO RESP-MESSAGE
               WHEN OTHER
                   MOVE 400 TO RESP-CODE
                   MOVE "Unsupported operation in repository" TO RESP-MESSAGE
           END-EVALUATE
           GOBACK.

