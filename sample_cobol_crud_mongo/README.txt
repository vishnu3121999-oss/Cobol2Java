Sample COBOL CRUD project (multi-file) with Mongo-style repository calls.

Files:
- CUSTOMER-API.cbl: request routing for CREATE/READ/UPDATE/DELETE/LIST.
- CUSTOMER-SERVICE.cbl: validation and service orchestration.
- CUSTOMER-REPOSITORY.cbl: simulated MongoDB adapter calls.
- copybooks/*.cpy: shared request/response/customer record structures.

Use this directory as input:
python main.py --target springboot --input-dir .\sample_cobol_crud_mongo --output-dir .\springboot-out --skip-review

