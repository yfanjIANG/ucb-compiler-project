class Person(object):

    def __init__(self: "Person", name: str, age: int):
        self.name = name
        self.age = age

    def greet(self: "Person") -> str:
        return "Hello, my name is " + self.name + " and I am " + str(self.age)

    def is_adult(self: "Person") -> bool:
        return self.age >= 18

class Student(Person):

    def __init__(self: "Student", name: str, age: int, student_id: int):
        super().__init__(name, age)
        self.student_id = student_id

    def greet(self: "Student") -> str:
        return super().greet() + ". I am a student with ID: " + str(self.student_id)

def main():
    i:int=5
    while i < 5:
        print(i)
        i = i + 1
    person1 = Person("Alice", 22)
    student1 = Student("Bob", 19, 12345)

    print(person1.greet())         # "Hello, my name is Alice and I am 22"
    print(student1.greet())        # "Hello, my name is Bob and I am 19. I am a student with ID: 12345"

    if person1.is_adult():
        print("Alice is an adult")
    elif student1.is_adult():
        print("Bob is an adult")
    else:
        print("Both are not adults")

    nums = [1, 2, 3, 4, 5]
    for num in nums:
        print(num * 2)
    

main()

