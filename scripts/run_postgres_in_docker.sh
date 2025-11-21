docker run -d --name postgres-ewm-stats-db		-e POSTGRES_DB=statsdb 		-p 5432:5432 -v postgres:/var/lib/postgres -e POSTGRES_PASSWORD=123456 -e POSTGRES_USER=dbuser postgres
docker run -d --name postgres-ewm-requests-db 	-e POSTGRES_DB=requests-db 	-p 5433:5432 -v postgres:/var/lib/postgres -e POSTGRES_PASSWORD=123456 -e POSTGRES_USER=dbuser postgres 
docker run -d --name postgres-ewm-main-db  		-e POSTGRES_DB=ewmdb   		-p 5434:5432 -v postgres:/var/lib/postgres -e POSTGRES_PASSWORD=123456 -e POSTGRES_USER=dbuser postgres
docker run -d --name postgres-ewm-users-db 	    -e POSTGRES_DB=users-db 	-p 5430:5432 -v postgres:/var/lib/postgres -e POSTGRES_PASSWORD=123456 -e POSTGRES_USER=dbuser postgres
docker run -d --name postgres-ewm-locations-db 	-e POSTGRES_DB=locations-db -p 5431:5432 -v postgres:/var/lib/postgres -e POSTGRES_PASSWORD=123456 -e POSTGRES_USER=dbuser postgres
