function execute(current_path, arguments)
    if #arguments == 0 then
        System:print("usage: mkdir <name>" .. "\n\n")
        return
    end


    local name = arguments[1];

    if not System:isDirectoryNameValid(name) then
        System:print("invalid directory name\n")
        return
    end

    local dir = System:createDirectory(name)

    if dir == nil then
        System:print("error creating directory " .. arguments[1] .. "\n\n")
        return
    end

    System:print("created directory " .. dir:getName() .. "\n\n")
end
