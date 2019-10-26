using Microsoft.Office.Core;
using Microsoft.Office.Interop.PowerPoint;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;

namespace PPTNotesExtractor
{
    class Extractor
    {
        static int Main(string[] args)
        {
            if (args.Length != 2)
            {
                Console.WriteLine("Extractor.exe <PowerPoint presentation> <json file>");
                return 1;
            }

            var extractor = new Extractor();
            var notes = extractor.Extract(args[0]);
            extractor.WriteJson(args[1], notes);

            return 0;
        }

        SlideNotes[] Extract(string filename)
        {
            List<SlideNotes> slideNotes = new List<SlideNotes>();
            Application app = new Application();
            Presentations presentations = app.Presentations;
            Presentation presentation = presentations.Open(filename);

            foreach (Slide slide in presentation.Slides)
            {
                if (slide.HasNotesPage != MsoTriState.msoTrue)
                {
                    continue;
                }

                List<string> list = new List<string>();
                SlideRange notesPages = slide.NotesPage;
                foreach (Microsoft.Office.Interop.PowerPoint.Shape shape in notesPages.Shapes)
                {
                    if (shape.Type == MsoShapeType.msoPlaceholder)
                    {
                        if (shape.PlaceholderFormat.Type == PpPlaceholderType.ppPlaceholderBody)
                        {
                            if (shape.HasTextFrame == MsoTriState.msoTrue)
                            {
                                if (shape.TextFrame.HasText == MsoTriState.msoTrue)
                                {
                                    var textRange = shape.TextFrame.TextRange;
                                    string[] lines = textRange.Text.Split(new[] { "\r\n", "\r", "\n" },
                                        StringSplitOptions.None
                                    );
                                    foreach (string line in lines)
                                    {
                                        if (line.Length > 0)
                                        {
                                            list.Add(line);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                SlideNotes current = new SlideNotes
                {
                    Name = slide.Name,
                    Notes = list.ToArray()
                };
                slideNotes.Add(current);
            }
            app.Quit();

            return slideNotes.ToArray();
        }

        void WriteJson(string filename, SlideNotes[] notes)
        {
            string json = JsonConvert.SerializeObject(notes);
            File.WriteAllText(filename, json);
        }
    }
}
