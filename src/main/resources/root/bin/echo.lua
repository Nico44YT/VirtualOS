function execute(fileSystem, currentDir, arguments)
    local str = ""

    for i, arg in ipairs(arguments) do
        str = str .. " " .. arg
    end

    print(str)
end

function description()
    return "Repeats whats inputed"
end