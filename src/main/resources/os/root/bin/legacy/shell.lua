function execute(current_path, arguments)
    System:setRawInput(true)

    local command = ""
    while true do
        System:clear()
        System:print(current_path .. "> " .. command)
        local input = System:awaitInput();

        if input == 8 then
            command = command:sub(1, #command - 1)

            goto continue
        end

        if input == 10 then
            local parts = split(command, " ", 2)
            if #parts == 1 then
                parts[2] = ""
            end

            local success = System:execute(parts[1], split(parts[2], " "))
            if not success then
                System:print("Unknown error occurred\n")
                goto continue
            end
        end

        local char = System:getCharacter(input)

        if char == nil then
            goto continue
        end

        if not System:isShiftDown() then
            char = char:lower()
        end

        command = command .. char

        ::continue::
    end

    System:setRawInput(false)
end

function split(str, sep, maxParts)
    sep = sep or "%s"
    local t = {}

    if not maxParts or maxParts <= 0 then
        for s in string.gmatch(str, "([^" .. sep .. "]+)") do
            t[#t + 1] = s
        end
        return t
    end

    local count = 0
    local pattern = "([^" .. sep .. "]+)"

    for s in string.gmatch(str, pattern) do
        count = count + 1

        if count == maxParts then
            -- append the rest of the string as the last element
            local start = string.find(str, s, 1, true)
            t[#t + 1] = string.sub(str, start)
            return t
        end

        t[#t + 1] = s
    end

    return t
end

function trim(s)
    return s:match("^%s*(.-)%s*$")
end