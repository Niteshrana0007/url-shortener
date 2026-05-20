.PHONY: help up down build test lint k8s-apply clean

help: ## Show available commands
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

up: ## Start all services (Docker Compose)
	docker-compose up --build -d

down: ## Stop all services
	docker-compose down

logs: ## Tail backend logs
	docker-compose logs -f backend

backend-build: ## Build backend JAR
	cd backend && mvn clean package -DskipTests

backend-test: ## Run backend tests
	cd backend && mvn clean verify

backend-run: ## Run backend locally
	cd backend && mvn spring-boot:run

frontend-install: ## Install frontend deps
	cd frontend && npm ci

frontend-dev: ## Start frontend dev server
	cd frontend && npm run dev

frontend-build: ## Build frontend for production
	cd frontend && npm run build

frontend-test: ## Run frontend tests
	cd frontend && npm run test

lint: ## Lint frontend
	cd frontend && npm run lint

k8s-apply: ## Apply K8s manifests (set IMAGE_TAG and ECR_REGISTRY first)
	kubectl apply -f k8s/base/ --namespace swiftlinkai

k8s-status: ## Check deployment status
	kubectl get pods,svc,hpa -n swiftlinkai

k8s-logs: ## Stream backend pod logs
	kubectl logs -f -l app=swiftlinkai-backend -n swiftlinkai

migrate: ## Run Flyway migrations manually
	cd backend && mvn flyway:migrate

sonar: ## Run SonarQube analysis
	cd backend && mvn sonar:sonar

clean: ## Clean build artefacts
	cd backend && mvn clean
	cd frontend && rm -rf dist node_modules/.cache
