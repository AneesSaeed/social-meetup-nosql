# Social Meetup NoSQL

## Overview

**Social Meetup NoSQL** is a social event platform built to demonstrate a **polyglot NoSQL architecture** combined with **event-driven integration**.

The system manages users and meetings while synchronizing data across multiple datastores using 
**Kafka and the Outbox Pattern** to ensure consistency.

---

## Architecture

| Component | Technology | Purpose |
|---------|-----------|---------|
| Core data | MongoDB | Users and meetings (source of truth) |
| Social graph | Neo4j | User relationships and recommendations |
| Search | Elasticsearch | User and meeting search |
| Cache | Redis | Performance optimization |
| Messaging | Kafka | Event propagation |

---

## Key Concepts

### Polyglot Persistence
Each datastore is used where it fits best:
- MongoDB for transactional data
- Neo4j for graph queries
- Elasticsearch for full-text search
- Redis for caching

---

### Event-Driven Architecture with Kafka

The system uses **Kafka** to propagate domain events such as:
- User created / updated
- Meeting created / updated / completed / cancelled

These events are consumed by:
- Neo4j consumers (update social graph)
- Elasticsearch consumers (update search indexes)

---

### Outbox Pattern

To avoid dual-write problems, the backend implements the **Outbox Pattern**:

1. Domain change is saved to MongoDB
2. An `OutboxEvent` is written in the same transaction
3. `OutboxEventPublisher` publishes the event to Kafka
4. Kafka consumers update Neo4j and Elasticsearch asynchronously

This guarantees **event delivery without data inconsistency**.



