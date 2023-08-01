# Use the official Go Alpine image as the base image
FROM golang:alpine

# Set the working directory inside the container
WORKDIR /app

# Install git
RUN apk add --no-cache git

# Copy the application files into the container
COPY . .

# Fetch all dependencies and put them in the vendor directory
RUN go mod tidy
RUN go mod vendor

# Build the Go application
RUN go build -o main .

# Set the command to run the application
CMD ["./main"]

# Expose the port the application listens on
EXPOSE 8080
