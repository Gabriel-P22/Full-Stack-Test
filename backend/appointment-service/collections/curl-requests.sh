#!/usr/bin/env bash
# Appointment Service - API requests
#
# Standalone curl commands for every endpoint of the appointment-service API.
# Run them directly from a terminal (with the app up on localhost:8080), or
# paste any individual command into Postman / Insomnia / Bruno via
# "Import > Raw text (curl)" to generate a request automatically.
#
# Replace <APPOINTMENT_ID> with an id returned by the create/list requests.
# Adjust the host if the app is running on a different port (see docker-compose.yml).

BASE_URL="http://localhost:8080/api/v1"

# ---------------------------------------------------------------------------
# 1. Create an appointment
#    Idempotency-Key is required; replaying the same key returns the same
#    appointment instead of creating a duplicate.
# ---------------------------------------------------------------------------
curl -X POST "$BASE_URL/appointment" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: 11111111-1111-1111-1111-111111111111" \
  -d '{
        "patientCpf": "52998224725",
        "patientName": "John Doe",
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
# 7. Update status -> back to PENDING
#    Republishes the appointment to Kafka; it gets asynchronously
#    reconfirmed (or auto-canceled if the slot/date is no longer valid).
# ---------------------------------------------------------------------------
curl -X PATCH "$BASE_URL/appointments/<APPOINTMENT_ID>/status" \
  -H "Content-Type: application/json" \
  -d '{
        "status": "PENDING"
      }'

# ---------------------------------------------------------------------------
# Error scenarios (useful for testing the error responses)
# ---------------------------------------------------------------------------

# Missing Idempotency-Key header -> 400 Bad Request
curl -X POST "$BASE_URL/appointment" \
  -H "Content-Type: application/json" \
  -d '{
        "patientCpf": "52998224725",
        "patientName": "John Doe",
        "scheduledAt": "2027-01-01T10:00:00"
      }'

# Invalid CPF -> 400 Bad Request
curl -X POST "$BASE_URL/appointment" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: 22222222-2222-2222-2222-222222222222" \
  -d '{
        "patientCpf": "12345678900",
        "patientName": "John Doe",
        "scheduledAt": "2027-01-01T10:00:00"
      }'

# Slot already taken (run the create request from step 1 twice with a
# different Idempotency-Key but the same scheduledAt) -> 409 Conflict

# Cancel without observation -> 409 Conflict
curl -X PATCH "$BASE_URL/appointments/<APPOINTMENT_ID>/status" \
  -H "Content-Type: application/json" \
  -d '{
        "status": "CANCELED"
      }'

# Unknown id -> 404 Not Found
curl -X GET "$BASE_URL/appointments/00000000-0000-0000-0000-000000000000"
