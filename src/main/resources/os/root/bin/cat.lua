function execute(current_path, arguments)
    if #arguments == 0 then
        System:print("usage: cat <file>" .. "\n")
        return
    end

    local content = System:readFile(arguments[1])
    if content == nil then
        System:print("file not found" .. "\n")
        return
    end

    System:print(content .. "\n\n")
end
