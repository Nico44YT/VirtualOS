function execute(current_path, arguments)
    if #arguments == 0 then
        System:print("usage: touch <filename>" .. "\n")
        return
    end

    local name = arguments[1];

    if not System:isFileNameValid(name) then
        System:print("invalid file name\n")
        return
    end

    local success = System:createFile(name)
    if success == nil then
        System:print("error" .. "\n")
        return
    end

    System:print(name .. " created\n")
end
