using Pkg
cd("julia_experiments")
pwd()
pkg"activate ."

files = walkdir("layer_jl") .|> (x->joinpath.(x[1], x[3])) |> Iterators.flatten |> collect

reg, func = regexes[2]
line = split(read(file, String), "\n")[19]
function replace_file(file)
    new_lines = []
    for line in split(read(file, String), "\n")
        print(line)
        for (reg, func) in regexes
            m = match(reg, line)
            if !isnothing(m)
                line = func(line, m)
                break
            end
        end
        push!(new_lines, line)
    end
    write(file, join(new_lines, "\n"))
end

types = Dict("int"=>"Int32","double"=>"Float64", "long"=>"Int64", "boolean"=>"Bool","void"=>"Nothing",
    "BiomeLayer"=>"BiomeLayer", "Sampler"=>"Sampler", "T"=>"T", "Biome"=>"Biome", "MCVersion"=>"MCVersion",
    "Type"=>"Type")

function replace_method(line, m)
    args = [split(var, " ")[1] for var in split(m.captures[5], ", ")[1:end-1]]
    new_vars = ""
    new_vars *= map(split(m.captures[5], ", ")[1:end-1]) do var
        var_type, var_name = split(var, " ")
        "$var_name::$(types[var_type])"
    end |> x->join(x,", ")
    var_type, var_name = split(m.captures[7], " ")
    new_vars *= "$var_name::$(types[var_type])"
    line_begin = line[1:m.offset-1]
    line_end = line[m.offsets[end]+length(m.captures[end]):end]
    "$(line_begin)function $(m.captures[4])(self, $new_vars)::$(types[m.captures[3]])$(line_end)"
end

function replace_method_no_args(line, m)
    m.captures
    line_begin = line[1:m.offset-1]
    line_end = line[m.offsets[end]+length(m.captures[end]):end]
    "$(line_begin)function $(m.captures[4])(self)::$(types[m.captures[3]])$(line_end)"
end

function replace_assignment(line, m)
    var_type, var_name = m.captures
    new_vars = "$var_name"
    line_begin = line[1:m.offset-1]
    line_end = line[m.offsets[end]+length(m.captures[end]):end]
    "$(line_begin)$new_vars$(line_end)"
end

function replace_end(line, m)
    line_begin = line[1:m.offsets[1]-1]
    line_end = line[m.offsets[end]+length(m.captures[end]):end]
    "$(line_begin)end$(line_end)"
end

regexes = [
    r"(public|private) (static )?(\w+) (\w+)\(((\w+ \w+, )*)(\w+ \w+)\) (\{)"=>replace_method,
    r"(public|private) (static )?(\w+) (\w+)\(\) (\{)"=>replace_method_no_args,
    r"(\w+) (\w+) = .*;"=>replace_assignment,
    r"^\s+(\})\s+$"=>replace_end
]

# rewrite_rules = [
#     r"(public|private) (static)? (\w+) (\w+)\((\w+ \w+, )*(\w+ \w+)\) \{"=>s"function \3(\4::Int, \5::Int, \6::Int)::Int",
#     "}"=>"end"
#     ]

files
files[1]
file = files[5]
[write(file, replace(read(file, String), r_w)) for r_w in rewrite_rules]
replace_file(file)
[replace_file(file) for file in files]
