# LWT Course Booking System

A multi-module Spring Boot course booking management system for sports venues.

## Architecture

```
lwt (parent)
├── lwt-common     # Utilities, exception handling, JWT, interceptor, configs
├── lwt-pojo       # Entity classes, DTOs, VOs
└── lwt-server     # Controllers, Services, Mappers, application entry point
```

## Database Design

| Table | Description |
|-------|-------------|
| `sys_user` | Users (student / coach / admin), stores `user_type` and `remaining_hours` |
| `venue` | Venue information (e.g. Court 1 - Court 5) |
| `coach_schedule` | Coach available time slots (set by coaches themselves) |
| `booking` | Student booking records |
| `recharge_record` | Manual hour recharge records |

## User Roles & Features

### Student
- View available schedules and coaches
- Book courses (conflict detection: venue/coach/student time)
- View my bookings and history
- View remaining course hours

### Coach
- Set available time slots
- View schedule and bookings
- Confirm bookings (auto-deducts 1 hour from student)

### Admin
- Manage venues (add/update)
- Manage users (add coaches/students)
- Manual recharge hours for students
- Dashboard: booking statistics by venue and coach

## API Endpoints

### Auth (public)
- `POST /auth/login` - Login
- `POST /auth/register` - Register (specify `userType`: student/coach/admin)
- `GET /auth/info` - Get user info

### Student (requires login)
- `GET /student/coaches` - List all coaches
- `GET /student/schedules?date=2026-05-01&coachId=1` - View available schedules
- `POST /student/book/{scheduleId}` - Book a course
- `GET /student/bookings?date=2026-05-01` - View my bookings
- `GET /student/history` - View completed bookings history
- `POST /student/cancel/{bookingId}` - Cancel a booking
- `GET /student/info` - View my info (including remaining hours)

### Coach (requires login)
- `POST /coach/schedule` - Add available time slot
- `GET /coach/schedules?date=2026-05-01` - View my schedules
- `DELETE /coach/schedule/{scheduleId}` - Delete a time slot
- `GET /coach/bookings?date=2026-05-01` - View bookings
- `POST /coach/confirm/{bookingId}` - Confirm booking (deducts 1 hour)

### Admin (requires login)
- `GET /admin/venues` - List venues
- `POST /admin/venue` - Add venue
- `PUT /admin/venue` - Update venue
- `GET /admin/coaches` - List coaches
- `GET /admin/students` - List students
- `POST /admin/user` - Add user
- `POST /admin/recharge` - Recharge hours for student
- `GET /admin/dashboard?startDate=2026-05-01&endDate=2026-05-31` - Dashboard stats

## Quick Start

### 1. Database Setup
```bash
mysql -u root -p < sql/schema.sql
```

### 2. Configuration
Edit `lwt-server/src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lwt?...
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

### 3. Run
```bash
mvn clean install
cd lwt-server
mvn spring-boot:run
```

### 4. Default Accounts
| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | Admin |
| coach1 | coach123 | Coach |
| coach2 | coach123 | Coach |
| student1 | student123 | Student (10 hours) |
| student2 | student123 | Student (5 hours) |

## Booking Conflict Detection Logic

When a student books a course, the system checks:
1. The schedule exists and is available
2. Student has remaining course hours
3. Student hasn't already booked this coach at this time
4. The venue isn't already booked at this time

## Tech Stack

- Spring Boot 3.2.5
- MyBatis-Plus 3.5.5
- MySQL 8.0
- Redis (token storage)
- JWT (authentication)
- Lombok
