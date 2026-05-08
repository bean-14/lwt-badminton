# LWT Course Booking System - API Documentation

## Base URL
```
http://localhost:8080
```

## Common Response Format
```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误/业务异常 |
| 401 | 未登录/token失效 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

## Authentication
所有需要登录的接口需在请求头携带：
```
Authorization: Bearer <token>
```

---

## 接口速查

### Auth 认证模块（无需登录）
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/login` | 用户登录 |
| POST | `/auth/register` | 用户注册 |
| GET | `/auth/info` | 获取当前用户信息 |

### Student 学生模块
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/student/schedules` | 查看可预约时段 |
| POST | `/student/book/{scheduleId}` | 预约课程 |
| GET | `/student/bookings` | 查看我的预约 |
| GET | `/student/history` | 查看上课历史 |
| POST | `/student/cancel/{bookingId}` | 取消预约 |
| POST | `/student/leave/{bookingId}` | 申请请假 |
| GET | `/student/coaches` | 查看所有教练 |
| GET | `/student/info` | 查看个人信息 |

### Coach 教练模块
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/coach/schedule` | 设置可预约时段 |
| GET | `/coach/schedules` | 查看我的排课 |
| DELETE | `/coach/schedule/{scheduleId}` | 删除排课时段 |
| GET | `/coach/bookings` | 查看预约列表 |
| POST | `/coach/confirm/{bookingId}` | 确认预约 |

### Admin 管理员模块
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/venues` | 获取场地列表 |
| POST | `/admin/venue` | 新增场地 |
| PUT | `/admin/venue` | 更新场地 |
| GET | `/admin/coaches` | 查看教练列表 |
| GET | `/admin/students` | 查看学生列表 |
| POST | `/admin/user` | 新增用户 |
| POST | `/admin/recharge` | 课时充值 |
| GET | `/admin/dashboard` | 数据看板 |
| GET | `/admin/stats/venue` | 场地使用统计 |
| GET | `/admin/stats/coach` | 教练上课统计 |
| GET | `/admin/stats/student` | 学生上课统计 |

---

## 1. Auth 认证模块

### 1.1 用户登录
**POST** `/auth/login`

> **说明**：用户登录获取 token，登录后所有需要权限的接口都依赖此 token

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码（明文，后端MD5加密） |

**请求示例**：
```json
{
  "username": "student1",
  "password": "student123"
}
```

**响应示例**：
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "token": "eyJhbGci...",
    "userId": 4,
    "userType": "student"
  }
}
```

---

### 1.2 用户注册
**POST** `/auth/register`

> **说明**：新用户注册，需指定 userType（student/coach/admin）

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名（不可重复） |
| password | String | 是 | 密码 |
| nickname | String | 否 | 昵称 |
| phone | String | 否 | 手机号 |
| userType | String | 是 | 用户类型：student / coach / admin |

**请求示例**：
```json
{
  "username": "student3",
  "password": "123456",
  "nickname": "小王",
  "phone": "13800138000",
  "userType": "student"
}
```

---

### 1.3 获取当前用户信息
**GET** `/auth/info`

> **说明**：根据 token 获取当前登录用户的详细信息

---

## 2. Student 学生模块

### 2.1 查看可预约时段
**GET** `/student/schedules`

> **说明**：查看所有教练的可预约时段，可按日期和教练筛选。学生据此发起预约

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| date | String | 否 | 日期，格式 yyyy-MM-dd |
| coachId | Long | 否 | 教练ID |

**响应示例**：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "coachId": 2,
      "coachName": "王教练",
      "venueId": 1,
      "venueName": "1号场",
      "scheduleDate": "2026-05-10",
      "startTime": "09:00",
      "endTime": "10:00",
      "bookedCount": 0,
      "status": 1
    }
  ]
}
```

---

### 2.2 预约课程
**POST** `/student/book/{scheduleId}`

> **说明**：学生对某个时段发起预约。系统自动进行冲突检测：
> 1. 时段是否存在且可用
> 2. 学生课时是否充足
> 3. 学生是否已预约该教练该时段
> 4. 该场地该时段是否已被其他学生占用

**预约成功条件**：以上检查全部通过，创建状态为 `pending`（待确认）的预约记录

| 参数 | 类型 | 说明 |
|------|------|------|
| scheduleId | Long (路径参数) | 可预约时段ID |

---

### 2.3 查看我的预约
**GET** `/student/bookings`

> **说明**：查看当前学生的所有预约记录（pending/confirmed/cancelled），可按日期筛选

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| date | String | 否 | 日期筛选 |

---

### 2.4 查看上课历史
**GET** `/student/history`

> **说明**：查看已完成（confirmed）的历史上课记录，按时间倒序

---

### 2.5 取消预约
**POST** `/student/cancel/{bookingId}`

> **说明**：取消 pending 状态的预约。已 confirmed 的预约不可取消

| 参数 | 类型 | 说明 |
|------|------|------|
| bookingId | Long (路径参数) | 预约记录ID |

---

### 2.6 申请请假
**POST** `/student/leave/{bookingId}?reason=xxx`

> **说明**：对已确认的预约申请请假。请假后课时不退还。

| 参数 | 类型 | 说明 |
|------|------|------|
| bookingId | Long (路径参数) | 预约记录ID |
| reason | String (查询参数) | 请假原因 |

---

### 2.7 查看所有教练
**GET** `/student/coaches`

> **说明**：获取所有状态正常的教练列表，用于选择教练查看其可预约时段

---

### 2.7 查看个人信息（含剩余课时）
**GET** `/student/info`

> **说明**：获取当前学生的详细信息，包含 `remainingHours`（剩余课时）

---

## 3. Coach 教练模块

### 3.1 设置可预约时段
**POST** `/coach/schedule`

> **说明**：教练设置自己未来可预约的时间段和场地。系统自动检测冲突：
> - 同一教练在该时段是否已有排课
> - 同一场地在该时段是否已被占用

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| coachId | Long | 是 | 教练ID（从token获取） |
| venueId | Long | 是 | 场地ID |
| scheduleDate | String | 是 | 日期 yyyy-MM-dd |
| startTime | String | 是 | 开始时间 HH:mm |
| endTime | String | 是 | 结束时间 HH:mm |

**请求示例**：
```json
{
  "coachId": 2,
  "venueId": 1,
  "scheduleDate": "2026-05-10",
  "startTime": "09:00",
  "endTime": "10:00"
}
```

---

### 3.2 查看我的排课
**GET** `/coach/schedules`

> **说明**：查看教练自己设置的所有可预约时段

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| date | String | 否 | 日期筛选 |

---

### 3.3 删除排课时段
**DELETE** `/coach/schedule/{scheduleId}`

> **说明**：删除自己设置的时段。如果该时段已有学生预约（pending），则不允许删除

---

### 3.4 查看预约列表
**GET** `/coach/bookings`

> **说明**：查看学生的预约申请，包括 pending（待确认）和 confirmed（已确认）的记录

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| date | String | 否 | 日期筛选 |

---

### 3.5 确认预约
**POST** `/coach/confirm/{bookingId}`

> **说明**：教练核实学生上课后点击确认。执行以下操作：
> 1. 将预约状态从 `pending` 改为 `confirmed`
> 2. **自动扣除学生 1 课时**（`remaining_hours - 1`）

| 参数 | 类型 | 说明 |
|------|------|------|
| bookingId | Long (路径参数) | 预约记录ID |

---

## 4. Admin 管理员模块

### 4.1 场地管理

#### 获取场地列表
**GET** `/admin/venues`

#### 新增场地
**POST** `/admin/venue`

| 参数 | 类型 | 说明 |
|------|------|------|
| name | String | 场地名称，如 "1号场" |
| location | String | 位置描述 |

#### 更新场地
**PUT** `/admin/venue`

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 场地ID |
| name | String | 场地名称 |
| location | String | 位置描述 |
| status | Integer | 状态 1=可用 0=不可用 |

---

### 4.2 用户管理

#### 查看教练列表
**GET** `/admin/coaches`

#### 查看学生列表
**GET** `/admin/students`

#### 新增用户
**POST** `/admin/user`

| 参数 | 类型 | 说明 |
|------|------|------|
| username | String | 用户名 |
| password | String | 密码 |
| nickname | String | 昵称 |
| phone | String | 手机号 |
| userType | String | student / coach / admin |

---

### 4.3 课时充值
**POST** `/admin/recharge`

> **说明**：管理员手动为学生充值课时

| 参数 | 类型 | 说明 |
|------|------|------|
| userId | Long | 学生ID |
| hours | Integer | 充值课时数 |

**请求示例**：
```json
{
  "userId": 4,
  "hours": 10
}
```

---

### 4.4 数据看板-
**GET** `/admin/dashboard`

> **说明**：预约统计数据，用于数据看板展示

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | String | 否 | 开始日期 |
| endDate | String | 否 | 结束日期 |

**响应示例**：
```json
{
  "code": 200,
  "data": {
    "totalBookings": 15,
    "venueStats": {
      "Court 1": 5,
      "Court 2": 4
    },
    "coachStats": {
      "王教练": 8,
      "李教练": 7
    }
  }
}
```

| 字段 | 说明 |
|------|------|
| totalBookings | 指定时间内已完成（confirmed）的预约总数 |
| venueStats | 各场地的预约次数统计 |
| coachStats | 各教练的预约次数统计 |

---

### 4.5 场地使用统计
**GET** `/admin/stats/venue`

> **说明**：获取各场地的使用次数统计（基于SQL GROUP BY，性能更优）

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | String | 否 | 开始日期 |
| endDate | String | 否 | 结束日期 |

**响应示例**：
```json
{
  "code": 200,
  "data": [
    {
      "venueName": "Court 1",
      "location": "Building A, Floor 1",
      "usageCount": 25
    },
    {
      "venueName": "Court 2",
      "location": "Building A, Floor 1",
      "usageCount": 18
    }
  ]
}
```

| 字段 | 说明 |
|------|------|
| venueName | 场地名称 |
| location | 场地位置 |
| usageCount | 使用次数（已确认的预约数） |

---

### 4.6 教练上课统计
**GET** `/admin/stats/coach`

> **说明**：获取各教练的上课次数统计

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | String | 否 | 开始日期 |
| endDate | String | 否 | 结束日期 |

**响应示例**：
```json
{
  "code": 200,
  "data": [
    {
      "coachId": 2,
      "coachName": "王教练",
      "classCount": 15
    },
    {
      "coachId": 3,
      "coachName": "李教练",
      "classCount": 10
    }
  ]
}
```

| 字段 | 说明 |
|------|------|
| coachId | 教练ID |
| coachName | 教练昵称 |
| classCount | 上课次数（已确认的预约数） |

---

### 4.7 学生上课统计
**GET** `/admin/stats/student`

> **说明**：获取各学生的上课次数统计

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | String | 否 | 开始日期 |
| endDate | String | 否 | 结束日期 |

**响应示例**：
```json
{
  "code": 200,
  "data": [
    {
      "studentId": 4,
      "studentName": "小张",
      "classCount": 8
    },
    {
      "studentId": 5,
      "studentName": "小陈",
      "classCount": 5
    }
  ]
}
```

| 字段 | 说明 |
|------|------|
| studentId | 学生ID |
| studentName | 学生昵称 |
| classCount | 上课次数（已确认的预约数） |

---

## 5. 预约状态说明

| 状态 | 说明 |
|------|------|
| `pending` | 学生已发起预约，等待教练确认 |
| `confirmed` | 教练已确认，课时已扣除 |
| `cancelled` | 预约已取消（仅 pending 状态可取消） |
| `leave` | 学生已请假（课时不退还） |

## 6. BookingVO 数据结构

用于预约相关接口返回的详细数据结构：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 预约ID |
| studentId | Long | 学生ID |
| studentName | String | 学生昵称 |
| coachId | Long | 教练ID |
| coachName | String | 教练昵称 |
| venueId | Long | 场地ID |
| venueName | String | 场地名称 |
| scheduleDate | String | 预约日期（yyyy-MM-dd） |
| status | String | 状态：pending/confirmed/cancelled/leave |
| createTime | String | 创建时间 |
| confirmTime | String | 确认时间 |
| leaveReason | String | 请假原因（仅leave状态有值） |
| leaveTime | String | 请假时间 |

## 7. 冲突检测逻辑

### 预约时（学生端）
```
1. 检查 coach_schedule 是否存在且状态可用
2. 检查学生 remainingHours > 0
3. 检查该学生同一时间是否已有其他预约
4. 检查该场地同一时间是否已被其他学生预约
```

### 设置排课时（教练端）
```
1. 检查该教练该时段是否已有排课
2. 检查该场地该时段是否已有排课
3. 检查时间段是否重叠（开始/结束时间交叉）
```
