$:.unshift File.dirname(__FILE__)

path = "../src/core/Logistic.java"
time = Time.new

f = File.new(path, "w")
f.puts("package core;")
f.puts("/** generated file */")
f.puts "public class Logistic {"
f.puts "  public static final String BUILD_DATE = \""+time.inspect+"\";"
f.puts "}"