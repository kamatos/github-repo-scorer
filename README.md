# GitHub Repository Scorer

A Spring Boot application that scores GitHub repositories based on multiple metrics including stars, forks, and
freshness. The application provides a REST API to search and score repositories with configurable scoring rules and
weights.

## Overview

This application fetches GitHub repositories based on search criteria and scores them using a composite scoring system.
Each repository is evaluated against three major scoring rules:

- **Stars**: Measures repository popularity using logarithmic normalization
- **Forks**: Measures community engagement using logarithmic normalization
- **Freshness**: Measures how recently the repository was updated using exponential decay


123


The final score is calculated as a weighted sum of these individual rule scores, providing a comprehensive assessment of
repository quality and activity.

## Technologies Used

- **Java 21** - Programming language
- **Spring Boot 3.5.5** - Application framework
- **Maven** - Build tool and dependency management
- **Docker** - Containerization
- **OpenAPI 3.0** - API specification and documentation
- **OpenAPI Generator** - Code generation based on OpenAPI spec
- **Lombok** - Boilerplate code generation
- **SpringDoc OpenAPI** - API documentation generation

### Main Endpoint

- `GET /api/v1/repositories/scores` - Score GitHub repositories based on search criteria

The API supports filtering by:

- `earliestCreationDate` - Filter repositories created after a specific date
- `language` - Filter by programming language
- `org` - Filter by organization name
- `repo` - Filter by repository name (partial match)

## How to Build

### Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- Docker (optional, for containerized deployment)

### Build Commands

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package the application
mvn clean package

# Build Docker image
mvn jib:dockerBuild
```

## How to Run

### Using Spring Boot

1. Set the GitHub API token:
   ```bash
   export GITHUB_API_TOKEN=your_github_token_here
   ```

2. Run the application:
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`

### Using Docker Compose

1. Create a `.env` file with your GitHub API token:
   ```bash
   echo "GITHUB_API_TOKEN=your_github_token_here" > .env
   ```

2. Build and run with Docker Compose:
   ```bash
   docker-compose up
   ```

The application will be available at `http://localhost:8080`

### Private Repository Access

The application supports searching private repositories and organizations by using a custom GitHub Personal Access
Token (PAT):

1. **Create a GitHub PAT** with appropriate permissions:
    - Go to GitHub Settings → Developer settings → Personal access tokens → Tokens (classic)
    - Generate a new token with the following scopes:
        - `repo` - Full control of private repositories
        - `read:org` - Read org and team membership
        - `read:user` - Read user profile data

2. **Set the token** as the `GITHUB_API_TOKEN` environment variable:
   ```bash
   export GITHUB_API_TOKEN=ghp_your_personal_access_token_here
   ```

3. **Search private organizations** by using the `org` parameter in API requests:
   ```bash
   curl "http://localhost:8080/api/v1/repositories/scores?org=your-private-org"
   ```

**Note**: When using a PAT with private repository access, you can search within private organizations that the token
has access to. The scoring caps should be adjusted based on the scope of repositories you're analyzing (e.g., for
organization-specific analysis, lower caps may be more appropriate).

## Implementation Details

### Repository Fetching and Filtering

The application fetches GitHub repositories using the GitHub REST API and applies the following filters:

- **earliestCreationDate**: Only includes repositories created on or after the specified date
- **repo**: Filters repositories by name using partial matching
- Additional filters for language and organization are also supported

### Scoring System

The scoring system uses three major rules that run in parallel using a composite pattern:

#### 1. Stars Scoring Rule

- Uses logarithmic normalization to compress large values
- Formula: `log1p(stars) / log1p(cap)`
- Default cap: 10,000 stars (tunable based on repository scope)
- Weight: 0.45 (45% of final score)

#### 2. Forks Scoring Rule

- Uses logarithmic normalization to compress large values
- Formula: `log1p(forks) / log1p(cap)`
- Default cap: 10,000 forks (tunable based on repository scope)
- Weight: 0.20 (20% of final score)

#### 3. Freshness Scoring Rule

- Uses exponential half-life formula for decay calculation
- Formula: `e^(-λ * days_since_update)` where `λ = ln(2) / half_life_days`
- Default half-life: 90 days (score halves after 90 days of inactivity)
- Weight: 0.35 (35% of final score)

### Scoring Configuration

The scoring rules are configurable through application properties:

```yaml
scoring:
  stars:
    cap: 10000        # Cap for logarithmic normalization
    weight: 0.45      # Weight in final score calculation
  forks:
    cap: 10000        # Cap for logarithmic normalization  
    weight: 0.20      # Weight in final score calculation
  freshness:
    half-life-days: 90  # Half-life in days for decay calculation
    weight: 0.35        # Weight in final score calculation
```

**Note**:

- For all public repositories, it's recommended to set the caps to 100,000 to better accommodate the full range of
  repository metrics
- For private organization analysis, lower caps (e.g., 1,000-10,000) may be more appropriate depending on the
  organization's repository scope
- Caps can be tuned based on the specific repository scope being analyzed

### Architecture

The scoring system uses a **Composite Pattern** for rule execution, where all rules run in parallel for optimal
performance. The implementation can be upgraded to use the **Chain of Responsibility Pattern** if business requirements
require sequential rule processing.

### Rule Weights

The default weights (0.45 for stars, 0.20 for forks, 0.35 for freshness) were chosen based on practical experience and
can be adjusted based on specific use cases or organizational requirements.

## API Documentation

The application exposes a REST API for repository scoring. For detailed API documentation, see
the [OpenAPI specification](github-repo-scorer.openapi.yaml).