function execute(current_path, arguments)
    System:clear()

    local credentialsFile = System:getFile("/conf/credentials.txt")
    local text = credentialsFile:getDataAsString()

    local parts = split(text, ";")
    if not parts[1] or not parts[2] then
        System:print("Error: credentials file is malformed\n")
        return
    end

    local username = split(parts[1], "=")
    local password = split(parts[2], "=")
    if not username[2] or not password[2] then
        System:print("Error: username or password missing in credentials file\n")
        return
    end

    System:print("Username:\n > ")
    local usernameInput = System:awaitInput()

    System:print("\nPassword:\n > ")
    local passwordInput = System:awaitInput()

    System:print("\n")

    usernameInput = trim(usernameInput)
    passwordInput = trim(passwordInput)
    local storedUsername = trim(username[2] or "")
    local storedPassword = trim(password[2] or "")

    if usernameInput == storedUsername and passwordInput == storedPassword then
        System:print("\nLogged in as " .. storedUsername .. "\n\n")
        System:print(System:getVersion() .. "\n")
        return
    end

    System:print("Incorrect username or password\n\n")
    System:sleep(1000)
    execute(current_path, arguments)
end

---@param str string
---@param sep string
---@return string[]
function split(str, sep)
    sep = sep or "%s"
    local t = {}
    for s in string.gmatch(str, "([^" .. sep .. "]+)") do
        t[#t + 1] = s
    end
    return t
end

function trim(s)
    return s:match("^%s*(.-)%s*$")
end