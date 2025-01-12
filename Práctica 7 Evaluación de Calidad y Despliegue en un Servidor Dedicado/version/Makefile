.PHONY: build run stop clean logs

build:
	./mvnw clean package -DskipTests
	docker-compose build

run:
	docker-compose up -d

stop:
	docker-compose down

clean:
	docker-compose down -v
	docker system prune -f

logs:
	docker-compose logs -f
