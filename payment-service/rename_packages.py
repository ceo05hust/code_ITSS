import os
import shutil

base_path = r"c:\Users\FPT\OneDrive\Documents\code_ITSS\payment-service\aims-backend"

# 1. Rename the test package directory if it exists
old_test_dir = os.path.join(base_path, "src", "test", "java", "com", "aims", "payment_service")
new_test_dir = os.path.join(base_path, "src", "test", "java", "com", "aims", "aimsbackend")

if os.path.exists(old_test_dir):
    print(f"Renaming test directory: {old_test_dir} -> {new_test_dir}")
    os.rename(old_test_dir, new_test_dir)

# 2. Replace all occurrences of com.aims.payment_service with com.aims.aimsbackend in all .java files
src_dir = os.path.join(base_path, "src")
for root, dirs, files in os.walk(src_dir):
    for file in files:
        if file.endswith(".java"):
            file_path = os.path.join(root, file)
            with open(file_path, "r", encoding="utf-8") as f:
                content = f.read()
            
            if "com.aims.payment_service" in content:
                print(f"Updating package in: {file_path}")
                updated_content = content.replace("com.aims.payment_service", "com.aims.aimsbackend")
                with open(file_path, "w", encoding="utf-8") as f:
                    f.write(updated_content)

print("Package renaming and string replacement completed successfully!")
