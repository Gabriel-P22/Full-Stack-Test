#!/usr/bin/env bash
# Appointment Service - API requests
#
# Standalone curl commands for every endpoint of the appointment-service API.
# Run them directly from a terminal (with the app up on localhost:8080), or
# paste any individual command into Postman / Insomnia / Bruno via
# "Import > Raw text (curl)" to generate a request automatically.
#
# Replace <APPOINTMENT_ID> with an id returned by the create/list requests.

BASE_URL="http://localhost:8080/api/v1"

# ---------------------------------------------------------------------------
# 1. Create an appointment
# ---------------------------------------------------------------------------
curl -X POST "$BASE_URL/appointments" \
  -H "Content-Type: application/json" \
  -d '{
        "patientName": "John Doe",
        "patientCpf": "52998224725",
        "scheduledAt": "2027-01-01T10:00:00"
      }'

# ---------------------------------------------------------------------------
# 2. List appointments (paginated, sorted by scheduledAt by default)
# ---------------------------------------------------------------------------
curl -X GET "$BASE_URL/appointments?page=0&size=10"

# ---------------------------------------------------------------------------
# 3. List appointments filtered by status (PENDING | CONFIRMED | CANCELED)
# ---------------------------------------------------------------------------
curl -X GET "$BASE_URL/appointments?status=PENDING"

# ---------------------------------------------------------------------------
# 4. Get a single appointment by id
# ---------------------------------------------------------------------------
curl -X GET "$BASE_URL/appointments/<APPOINTMENT_ID>"

# ---------------------------------------------------------------------------
# 5. Update status -> confirm a pending appointment
# ---------------------------------------------------------------------------
curl -X PATCH "$BASE_URL/appointments/<APPOINTMENT_ID>/status" \
  -H "Content-Type: application/json" \
  -d '{
        "status": "CONFIRMED"
      }'

# ---------------------------------------------------------------------------
# 6. Update status -> cancel an appointment (observation is required)
# ---------------------------------------------------------------------------
curl -X PATCH "$BASE_URL/appointments/<APPOINTMENT_ID>/status" \
  -H "Content-Type: application/json" \
  -d '{
        "status": "CANCELED",
        "observation": "Patient requested cancellation"
      }'

# ---------------------------------------------------------------------------
# Error scenarios (useful for testing the error responses)
# ---------------------------------------------------------------------------

# Invalid CPF (not exactly 11 digits) -> 400 Bad Request
curl -X POST "$BASE_URL/appointments" \
  -H "Content-Type: application/json" \
  -d '{
        "patientName": "John Doe",
        "patientCpf": "12345",
        "scheduledAt": "2027-01-01T10:00:00"
      }'

# Date in the past -> 400 Bad Request
curl -X POST "$BASE_URL/appointments" \
  -H "Content-Type: application/json" \
  -d '{
        "patientName": "John Doe",
        "patientCpf": "52998224725",
        "scheduledAt": "2020-01-01T10:00:00"
      }'

# Cancel without observation -> 400 Bad Request
curl -X PATCH "$BASE_URL/appointments/<APPOINTMENT_ID>/status" \
  -H "Content-Type: application/json" \
  -d '{
        "status": "CANCELED"
      }'

# Change status of an already-canceled appointment -> 409 Conflict
# (run the cancel request above twice against the same <APPOINTMENT_ID>)

# Unknown id -> 404 Not Found
curl -X GET "$BASE_URL/appointments/00000000-0000-0000-0000-000000000000"
