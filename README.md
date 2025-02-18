# Redis Persistence Framework

## Project Overview

A custom Java persistence framework that leverages reflection and annotations to seamlessly store and retrieve objects in Redis. This framework demonstrates advanced Java concepts including runtime annotations, reflection, and dynamic proxies while following clean architecture principles.

## Key Features

- **Annotation-based persistence** (`@Persistable`, `@PersistableField`, `@PersistableId`)
- **Generic object storage and retrieval** using reflection
- **List field support** with automatic relationship management
- **Lazy loading capabilities** using dynamic proxies
- **Session-based persistence management**
- **Redis integration** for data storage

## Technical Implementation

- **Reflection API**: Utilizes Java's reflection capabilities to inspect and manipulate objects at runtime.
- **Custom Annotations**: Implements runtime annotations for marking persistable classes and fields.
- **Clean Architecture**: Maintains separation of concerns between persistence logic and domain objects.

## Design Patterns:

- **Proxy Pattern** for lazy loading
- **Session Pattern** for managing persistence operations
- **Repository Pattern** for data access abstraction

## Technologies Used

- **Java**
- **Redis**
- **Jedis** (Redis Java Client)
- **Javassist** (for dynamic proxy generation)

## Best Practices Demonstrated

- **Interface Segregation Principle** through focused annotation definitions
- **Single Responsibility Principle** in the Session class design
- **Open/Closed Principle** through extensible annotation system
- **Dependency Inversion** using reflection for loose coupling
- **Clean Code principles** with clear separation of concerns
