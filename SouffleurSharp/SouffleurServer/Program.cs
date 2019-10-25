using Microsoft.Office.Core;
using Microsoft.Office.Interop.PowerPoint;
using System;
using System.Collections;

namespace SouffleurServer
{
    class Program
    {
        static void Main(string[] args)
        {
            Application app = new Application();
            Presentations presentations = app.Presentations;
            Presentation presentation = presentations.Open(@"C:\Users\thoma\Test-Präsentation.pptx");

            foreach (Slide slide in presentation.Slides)
            {
                if (slide.HasNotesPage != MsoTriState.msoTrue)
                {
                    continue;
                }

                Console.WriteLine(slide.Name);
                ArrayList list = new ArrayList();
                Microsoft.Office.Interop.PowerPoint.SlideRange notesPages = slide.NotesPage;
                foreach (Microsoft.Office.Interop.PowerPoint.Shape shape in notesPages.Shapes)
                {
                    if (shape.Type == MsoShapeType.msoPlaceholder)
                    {
                        if (shape.PlaceholderFormat.Type == Microsoft.Office.Interop.PowerPoint.PpPlaceholderType.ppPlaceholderBody)
                        {
                            if (shape.HasTextFrame == MsoTriState.msoTrue)
                            {
                                if (shape.TextFrame.HasText == MsoTriState.msoTrue)
                                {
                                    var textRange = shape.TextFrame.TextRange;
                                    var text = textRange.Text;
                                    list.Add(text);
                                }
                            }
                        }
                    }
                }
                foreach (string s in list)
                {
                    Console.WriteLine($"   - {s}");
                }
            }
            app.Quit();
        }
    }
}
