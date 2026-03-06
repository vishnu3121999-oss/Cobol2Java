       IDENTIFICATION DIVISION.
       PROGRAM-ID. RESTAPI-SAMPLE.

       ENVIRONMENT DIVISION.

       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  REQUEST-METHOD        PIC X(10) VALUE "GET".
       01  REQUEST-PATH          PIC X(30) VALUE "/customers".
       01  REQUEST-BODY          PIC X(200) VALUE SPACES.
       01  RESPONSE-STATUS       PIC 9(3) VALUE 200.
       01  RESPONSE-BODY         PIC X(500) VALUE SPACES.
       01  WS-CUSTOMER-ID        PIC 9(5) VALUE 10001.
       01  WS-CUSTOMER-NAME      PIC X(30) VALUE "JOHN DOE".

       PROCEDURE DIVISION.
       MAIN-PROCESS.
           PERFORM ROUTE-REQUEST
           PERFORM PRINT-RESPONSE
           STOP RUN.

       ROUTE-REQUEST.
           EVALUATE TRUE
               WHEN REQUEST-METHOD = "GET"
                    AND REQUEST-PATH = "/health"
                   MOVE 200 TO RESPONSE-STATUS
                   MOVE "{""status"":""UP""}" TO RESPONSE-BODY
               WHEN REQUEST-METHOD = "GET"
                    AND REQUEST-PATH = "/customers"
                   PERFORM GET-CUSTOMER-LIST
               WHEN REQUEST-METHOD = "POST"
                    AND REQUEST-PATH = "/customers"
                   PERFORM CREATE-CUSTOMER
               WHEN OTHER
                   MOVE 404 TO RESPONSE-STATUS
                   MOVE "{""error"":""Not Found""}" TO RESPONSE-BODY
           END-EVALUATE.

       GET-CUSTOMER-LIST.
           MOVE 200 TO RESPONSE-STATUS
           STRING
               "{""customers"":[{""id"":" DELIMITED BY SIZE
               WS-CUSTOMER-ID DELIMITED BY SIZE
               ",""name"":""" DELIMITED BY SIZE
               WS-CUSTOMER-NAME DELIMITED BY SIZE
               """}]}" DELIMITED BY SIZE
               INTO RESPONSE-BODY
           END-STRING.

       CREATE-CUSTOMER.
           MOVE 201 TO RESPONSE-STATUS
           MOVE "{""message"":""Customer created""}" TO RESPONSE-BODY.

       PRINT-RESPONSE.
           DISPLAY "HTTP STATUS: " RESPONSE-STATUS
           DISPLAY "BODY: " RESPONSE-BODY.

