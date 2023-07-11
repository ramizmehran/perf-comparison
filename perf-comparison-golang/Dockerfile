# Dockerfile

# Use the official Go Alpine image as the base image
FROM golang:alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the application files into the container
COPY . .

# Install required packages
RUN apk add --no-cache git
RUN go get github.com/jmoiron/sqlx

# Set the environment variables for MySQL and Redis endpoints
ENV MYSQL_ENDPOINT=mysql-container
ENV REDIS_ENDPOINT=redis-container

# Build the Go application
RUN go build -o main .

# Set the command to run the application
CMD ["./main"]

# Expose the port the application listens on
EXPOSE 8080
