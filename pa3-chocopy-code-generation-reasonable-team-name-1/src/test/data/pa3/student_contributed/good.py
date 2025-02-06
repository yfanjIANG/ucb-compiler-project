def list_add(y:[int], z:[int])->[int]:
    return y+z

def str_add(y:str, z:str)->str:
    return y+z


class Animals(object):
    name:str = "animals"
    def printName(self:"Animals"):
        print(self.name)
    
class Cat(Animals):
    b:str = "cat"
    def printName(self: "Cat"):
        print(self.b)
 

    
c:Animals = None
d:Cat = None
x:int = 0
y:[int] = None
z:[int] = None
ans:[int] = None
u:str = "123"
v:str = "456"
a:int = 4
b:int = 2


c = Animals()
d = Cat()
y = [1, 2, 3]
z = [4, 5, 6]
ans = list_add(y, z)


#test list
for x in ans:
    print(x)
    
#test object    
c.printName()
d.printName()

#test string    
print(str_add(u,v))

#test arithmetic
print(a + b)
print(a - b)
print(a // b)
print(a * b)
print(a % b)
print(a == b)
print(a > b)
print(a >= b)
print(a < b)
print(a <= b)