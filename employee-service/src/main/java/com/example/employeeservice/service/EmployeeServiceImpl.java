package com.example.employeeservice.service;

import com.example.employeeservice.entity.Employee;
import com.example.employeeservice.messaging.EmployeeMessageProducer;
import com.example.employeeservice.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service

public class EmployeeServiceImpl implements EmployeeService {
	@Autowired
    private EmployeeRepository repository;

    @Autowired
    private EmployeeMessageProducer messageProducer;
    @Override
    public List<Employee> getAllEmployees() {
        return repository.findAll();
    }

    @Override
    public Optional<Employee> getEmployeeById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Employee createEmployee(Employee employee) {
        return repository.save(employee);
    }

    @Override
    public Employee updateEmployee(Long id, Employee employee) {
        if (repository.existsById(id)) {
            employee.setId(id);
            Employee updatedEmployee = repository.save(employee);
            messageProducer.sendEmployeeUpdate(employee.getId());
            return updatedEmployee;
        }
        return null;
    }

    @Override
    public void deleteEmployee(Long id) {
        repository.deleteById(id);
    }
}