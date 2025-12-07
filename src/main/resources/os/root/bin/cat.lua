function execute(os, current_path, arguments)
    if arguments == nil or arguments == "" then
        os:print("Usage: cat <filename>\n")
        return
    end

    local fullPath = current_path .. arguments

    local file = os:getFile(fullPath)

    if file == nil then
        os:print("cat: '" .. arguments .. "' not found\n")
        return
    end

    local data = file:getData()

    os:print("\n" .. data .. "\n")
end